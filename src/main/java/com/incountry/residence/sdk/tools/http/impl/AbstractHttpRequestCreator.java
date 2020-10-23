package com.incountry.residence.sdk.tools.http.impl;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import com.incountry.residence.sdk.tools.containers.RequestParameters;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public abstract class AbstractHttpRequestCreator {
    private static final Logger LOG = LogManager.getLogger(AbstractHttpRequestCreator.class);

    private static final String MSG_ERR_URL = "URL error";
    private static final String MSG_ERR_SERVER_REQUEST = "Server request error: %s";
    private static final String MSG_ERR_NULL_BODY = "Body can't be null";

    private static final String POST = "POST";
    private static final String GET = "GET";
    private static final String PATCH = "PATCH";
    private static final String FILE = "file";

    protected HttpRequestBase createRequest(String url, String method, String body, RequestParameters requestParameters) throws UnsupportedEncodingException, StorageServerException {
        if (requestParameters != null && requestParameters.isFileUpload()) {
            return createFileUploadRequest(url, method, body, requestParameters.getFileName(), requestParameters.getContentType());
        } else {
            return createSimpleRequest(url, method, body);
        }
    }

    private HttpRequestBase createSimpleRequest(String url, String method, String body) throws UnsupportedEncodingException, StorageServerException {
        URI uri = createUri(url);

        if (method.equals(POST)) {
            checkBodyForNull(body, method);
            HttpPost request = new HttpPost(uri);
            StringEntity entity = new StringEntity(body);
            request.setEntity(entity);
            return request;
        } else if (method.equals(GET)) {
            return new HttpGet(uri);
        } else if (method.equals(PATCH)) {
            checkBodyForNull(body, method);
            HttpPatch request = new HttpPatch(uri);
            StringEntity entity = new StringEntity(body);
            request.setEntity(entity);
            return request;
        } else {
            return new HttpDelete(uri);
        }
    }

    private HttpRequestBase createFileUploadRequest(String url, String method, String body, String fileName, String mimeTypeString) throws StorageServerException {
        checkBody(body, method);
        URI uri = createUri(url);
        ContentType mimeType;
        if (mimeTypeString == null || mimeTypeString.isEmpty()) {
            mimeType = ContentType.DEFAULT_BINARY;
        } else {
            mimeType = ContentType.create(mimeTypeString);
        }

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.setCharset(StandardCharsets.UTF_8);
        builder.addBinaryBody(FILE, body.getBytes(StandardCharsets.UTF_8), mimeType, fileName);
        HttpEntity entity = builder.build();

        if (method.equals(POST)) {
            HttpPost request = new HttpPost(uri);
            request.setEntity(entity);
            return request;
        } else {
            HttpPut request = new HttpPut(uri);
            request.setEntity(entity);
            return request;
        }
    }

    private void checkBody(String body, String method) throws StorageServerException {
        checkBodyForNull(body, method);
        if (body.isEmpty()) {
            LOG.error(MSG_ERR_NULL_BODY);
            throw new StorageServerException(String.format(MSG_ERR_SERVER_REQUEST, method), new StorageClientException(MSG_ERR_NULL_BODY));
        }
    }

    private void checkBodyForNull(String body, String method) throws StorageServerException {
        if (body == null) {
            LOG.error(MSG_ERR_NULL_BODY);
            throw new StorageServerException(String.format(MSG_ERR_SERVER_REQUEST, method), new StorageClientException(MSG_ERR_NULL_BODY));
        }
    }

    private URI createUri(String url) throws StorageServerException {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException ex) {
            throw new StorageServerException(MSG_ERR_URL, ex);
        }
        return uri;
    }
}
