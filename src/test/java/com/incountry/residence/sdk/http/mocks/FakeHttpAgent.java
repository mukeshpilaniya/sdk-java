package com.incountry.residence.sdk.http.mocks;

import com.incountry.residence.sdk.tools.models.HttpParameters;
import com.incountry.residence.sdk.tools.models.ApiResponse;
import com.incountry.residence.sdk.tools.http.HttpAgent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeHttpAgent implements HttpAgent {

    private String callUrl;
    private String callMethod;
    private String callBody;
    private String response;
    private String callRegion;
    private List<String> responseList;
    private Map<Integer, com.incountry.residence.sdk.tools.dao.impl.ApiResponse> codeMap;
    private int retryCount;
    private String audienceUrl;

    public FakeHttpAgent(String response) {
        this.response = response;
    }

    public FakeHttpAgent(List<String> responseList) {
        this.responseList = responseList;
    }

    @Override
    public ApiResponse request(String url, String body, String audience, String region, int retryCount, HttpParameters httpParameters) {
        this.callUrl = url;
        this.callMethod = httpParameters.getMethod();
        this.callBody = body;
        this.codeMap = httpParameters.getCodeMap();
        this.retryCount = retryCount;
        this.audienceUrl = audience;
        this.callRegion = region;
        return new ApiResponse(getResponse(), new HashMap<>());
    }

    public String getCallUrl() {
        return callUrl;
    }

    public String getCallMethod() {
        return callMethod;
    }

    public String getCallBody() {
        return callBody;
    }

    public String getAudienceUrl() {
        return audienceUrl;
    }

    public Map<Integer, com.incountry.residence.sdk.tools.dao.impl.ApiResponse> getCodeMap() {
        return codeMap;
    }

    public String getCallRegion() {
        return callRegion;
    }

    public String getResponse() {
        if (responseList != null && !responseList.isEmpty()) {
            response = responseList.get(0);
            if (responseList.size() == 1) {
                responseList = null;
            } else {
                responseList = responseList.subList(1, responseList.size());
            }
        }
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public int getRetryCount() {
        return retryCount;
    }
}
