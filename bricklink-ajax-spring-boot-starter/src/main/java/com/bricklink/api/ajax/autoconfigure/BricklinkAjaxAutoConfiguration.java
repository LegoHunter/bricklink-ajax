package com.bricklink.api.ajax.autoconfigure;

import com.bricklink.api.ajax.BricklinkAjaxClient;
import com.bricklink.api.ajax.client.BricklinkAjaxHttpClient;
import com.bricklink.api.ajax.client.DefaultBricklinkAjaxClient;
import com.bricklink.api.ajax.client.RateLimitedBricklinkAjaxHttpClient;
import com.bricklink.api.ajax.exception.BricklinkAjaxClientException;
import com.bricklink.api.ajax.exception.BricklinkAjaxServerException;
import com.bricklink.api.ajax.ratelimit.BricklinkAjaxRateLimiter;
import com.bricklink.api.ajax.ratelimit.ThreadSleeper;
import com.bricklink.api.ajax.support.BricklinkAjaxHttpLoggingInterceptor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;

@AutoConfiguration
@EnableConfigurationProperties(BricklinkAjaxProperties.class)
public class BricklinkAjaxAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "bricklinkAjaxObjectMapper")
    public ObjectMapper bricklinkAjaxObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    @Bean
    @ConditionalOnMissingBean(name = "bricklinkAjaxClientDelegate")
    public RestClient bricklinkAjaxClientDelegate(
            BricklinkAjaxProperties properties,
            @Qualifier("bricklinkAjaxObjectMapper") ObjectMapper objectMapper
    ) {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(properties.getUri().toString())
                .messageConverters(converters -> {
                    converters.removeIf(MappingJackson2HttpMessageConverter.class::isInstance);
                    converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
                })
                .defaultStatusHandler(statusCode -> statusCode.is3xxRedirection() || statusCode.isError(), (request, response) -> {
                    int statusCode = response.getStatusCode().value();
                    String body = responseBody(response);
                    String requestDescription = request.getMethod() + " " + request.getURI();
                    if (statusCode >= 300 && statusCode <= 499) {
                        if (statusCode >= 300 && statusCode <= 399) {
                            body = "Unexpected redirect to [%s]. Response body: [%s]".formatted(
                                    response.getHeaders().getFirst(HttpHeaders.LOCATION),
                                    body
                            );
                        }
                        throw new BricklinkAjaxClientException(statusCode, "%s returned [%s]".formatted(requestDescription, body));
                    }
                    throw new BricklinkAjaxServerException(statusCode, "%s returned [%s]".formatted(requestDescription, body));
                });

        if (properties.getHttpLogging().isEnabled()) {
            builder.requestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
            builder.requestInterceptor(new BricklinkAjaxHttpLoggingInterceptor());
        }

        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "rawBricklinkAjaxHttpClient")
    public BricklinkAjaxHttpClient rawBricklinkAjaxHttpClient(
            @Qualifier("bricklinkAjaxClientDelegate") RestClient bricklinkAjaxClientDelegate
    ) {
        RestClientAdapter adapter = RestClientAdapter.create(bricklinkAjaxClientDelegate);
        return HttpServiceProxyFactory.builderFor(adapter)
                .build()
                .createClient(BricklinkAjaxHttpClient.class);
    }

    @Bean
    @ConditionalOnMissingBean
    public BricklinkAjaxRateLimiter bricklinkAjaxRateLimiter(BricklinkAjaxProperties properties) {
        return new BricklinkAjaxRateLimiter(
                Duration.ofMillis(properties.getRateLimit().getMinimumDelayMs()),
                Clock.systemUTC(),
                ThreadSleeper.INSTANCE
        );
    }

    @Bean
    @ConditionalOnMissingBean(name = "bricklinkAjaxHttpClient")
    public BricklinkAjaxHttpClient bricklinkAjaxHttpClient(
            BricklinkAjaxProperties properties,
            @Qualifier("rawBricklinkAjaxHttpClient") BricklinkAjaxHttpClient rawBricklinkAjaxHttpClient,
            BricklinkAjaxRateLimiter bricklinkAjaxRateLimiter
    ) {
        if (!properties.getRateLimit().isEnabled()) {
            return rawBricklinkAjaxHttpClient;
        }
        return new RateLimitedBricklinkAjaxHttpClient(rawBricklinkAjaxHttpClient, bricklinkAjaxRateLimiter);
    }

    @Bean
    @ConditionalOnMissingBean
    public BricklinkAjaxClient bricklinkAjaxClient(
            @Qualifier("bricklinkAjaxHttpClient") BricklinkAjaxHttpClient bricklinkAjaxHttpClient
    ) {
        return new DefaultBricklinkAjaxClient(bricklinkAjaxHttpClient);
    }

    private static String responseBody(ClientHttpResponse response) throws IOException {
        return StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
    }
}
