package com.bricklink.api.ajax.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class BricklinkAjaxHttpLoggingInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        log.debug("bricklink_ajax.request method={} uri={}", request.getMethod(), request.getURI());
        ClientHttpResponse response = execution.execute(request, body);
        String responseBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        log.debug("bricklink_ajax.response status={} body={}", response.getStatusCode(), responseBody);
        return response;
    }
}
