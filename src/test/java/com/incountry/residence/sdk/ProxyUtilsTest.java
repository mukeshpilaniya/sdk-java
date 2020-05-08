package com.incountry.residence.sdk;

import com.incountry.residence.sdk.tools.dao.impl.ApiResponse;
import com.incountry.residence.sdk.tools.http.HttpAgent;
import com.incountry.residence.sdk.tools.proxy.ProxyUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProxyUtilsTest {

    static class FakeHttpAgent implements HttpAgent {
        @Override
        public String request(String endpoint, String method, String body, Map<Integer, ApiResponse> codeMap, String token) {
            doNothing();
            throw new NullPointerException();
        }

        private void doNothing() {
        }
    }

    @Test
    public void testProxyException() {
        HttpAgent agent = ProxyUtils.createLoggingProxyForPublicMethods(new FakeHttpAgent());
        assertThrows(NullPointerException.class, () -> agent.request(null, null, null, null, null));
    }
}
