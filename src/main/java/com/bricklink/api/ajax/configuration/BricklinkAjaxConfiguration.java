package com.bricklink.api.ajax.configuration;

import com.bricklink.api.ajax.BricklinkAjaxClient;
import com.bricklink.api.ajax.PagingBricklinkAjaxClient;
import com.bricklink.web.support.BricklinkWebService;
import feign.Feign;
import feign.Logger;
import feign.httpclient.ApacheHttpClient;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class BricklinkAjaxConfiguration {

    @Bean
    public PagingBricklinkAjaxClient pagingBricklinkAjaxClient(final BricklinkAjaxClient bricklinkAjaxClient) {
        return new PagingBricklinkAjaxClient(bricklinkAjaxClient);
    }

    @Bean
    public BricklinkAjaxClient bricklinkAjaxClient(BricklinkWebService bricklinkWebService) {
        return Feign
                .builder()
                .client(new ApacheHttpClient(bricklinkWebService.getHttpClient()))
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .logger(new Slf4jLogger(BricklinkAjaxClient.class))
                .logLevel(Logger.Level.FULL)
                .target(BricklinkAjaxClient.class, "https://www.bricklink.com");
    }
}
