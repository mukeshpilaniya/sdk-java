package com.incountry.residence.sdk.tools.http.impl;

import com.incountry.residence.sdk.tools.models.CustomEnum;
import com.incountry.residence.sdk.tools.models.HttpParameters;
import com.incountry.residence.sdk.tools.models.ApiResponse;
import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.http.HttpAgent;
import com.incountry.residence.sdk.tools.http.TokenClient;
import com.incountry.residence.sdk.version.Version;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class HttpAgentImpl extends AbstractHttpRequestCreator implements HttpAgent {

    private static final Logger LOG = LogManager.getLogger(HttpAgentImpl.class);

    private static final String MSG_SERVER_ERROR = "Server request error: [URL=%s, method=%s]";
    private static final String MSG_URL_NULL_ERR = "URL can't be null";
    private static final String MSG_ERR_CONTENT = "Code=%d, endpoint=[%s], content=[%s]";
    private static final String BEARER = "Bearer ";
    private static final String AUTHORIZATION = "Authorization";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String ENV_ID = "x-env-id";
    private static final String USER_AGENT = "User-Agent";
    private static final String ATTACHMENTS = "attachments";
    private static final String META = "meta";
    private static final String METHOD_GET = "meta";

    private final TokenClient tokenClient;
    private final String environmentId;
    private final String userAgent;
    private final CloseableHttpClient httpClient;


    public HttpAgentImpl(TokenClient tokenClient, String environmentId, CloseableHttpClient httpClient) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("HttpAgentImpl constructor params (tokenClient={} , environmentId={})",
                    tokenClient,
                    environmentId != null ? "[SECURE[" + environmentId.hashCode() + "]]" : null);
        }
        this.tokenClient = tokenClient;
        this.environmentId = environmentId;
        this.userAgent = "SDK-Java/" + Version.BUILD_VERSION;
        this.httpClient = httpClient;
    }

    @Override
    public ApiResponse request(String url, String body,
                               String audience, String region, int retryCount, HttpParameters httpParameters) throws StorageServerException, StorageClientException {
        String method = httpParameters.getMethod();
        Map<Integer, com.incountry.residence.sdk.tools.dao.impl.ApiResponse> codeMap = httpParameters.getCodeMap();
        if (LOG.isTraceEnabled()) {
            LOG.trace("HTTP request params (url={} , method={} , codeMap={})",
                    url,
                    method,
                    codeMap);
        }
        if (url == null) {
            throw new StorageClientException(MSG_URL_NULL_ERR);
        }
        try {
            HttpRequestBase request = createRequest(url, method, body);
            addHeaders(request, audience, region, httpParameters.getContentType());
            CloseableHttpResponse response = httpClient.execute(request);

            int status = response.getStatusLine().getStatusCode();
            HttpEntity responseEntity = response.getEntity();
            Map<CustomEnum, String> metaInfo = new EnumMap<>(CustomEnum.class);
            if (ContentType.get(responseEntity) != null && isFileDownloadRequest(url, httpParameters.getMethod())) {
                String fileExtension = ContentType.get(responseEntity).getMimeType().split("/")[1];
                metaInfo.put(CustomEnum.EXTENSION, fileExtension);
            }
            String actualResponseContent = EntityUtils.toString(response.getEntity());
            response.close();
            com.incountry.residence.sdk.tools.dao.impl.ApiResponse expectedResponse = codeMap.get(status);
            boolean isSuccess = expectedResponse != null && !expectedResponse.isError() && !actualResponseContent.isEmpty();
            boolean isFinish = isSuccess || expectedResponse == null || !canRetry(expectedResponse, retryCount);
            if (!isFinish) {
                tokenClient.refreshToken(true, audience, region);
                return request(url, body, audience, region, retryCount - 1, httpParameters);
            }
            if (expectedResponse != null && expectedResponse.isIgnored()) {
                return new ApiResponse(null);
            }
            if (expectedResponse == null || expectedResponse.isError()) {
                String errorMessage = String.format(MSG_ERR_CONTENT, status, url, actualResponseContent).replaceAll("[\r\n]", "");
                LOG.error(errorMessage);
                throw new StorageServerException(errorMessage);
            }
            return new ApiResponse(actualResponseContent, metaInfo);
        } catch (IOException ex) {
            String errorMessage = String.format(MSG_SERVER_ERROR, url, method);
            throw new StorageServerException(errorMessage, ex);
        }
    }

    private boolean isFileDownloadRequest(String url, String method) {
        return url.contains(ATTACHMENTS) && !url.endsWith(META) && method.equals(METHOD_GET);
    }

    private HttpRequestBase addHeaders(HttpRequestBase request, String audience, String region, String contentType) throws StorageServerException {
        if (audience != null) {
            request.addHeader(AUTHORIZATION, BEARER + tokenClient.getToken(audience, region));
        }
        request.addHeader(CONTENT_TYPE, contentType);
        request.addHeader(ENV_ID, environmentId);
        request.addHeader(USER_AGENT, userAgent);

        return request;
    }

    private boolean canRetry(com.incountry.residence.sdk.tools.dao.impl.ApiResponse params, int retryCount) {
        return params.isCanRetry() && retryCount > 0;
    }
}
