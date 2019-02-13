package com.bricklink.api.ajax.configuration;

import com.bricklink.api.ajax.BricklinkAjaxClient;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class BricklinkAjaxConfiguration {

    @Bean
    public BricklinkAjaxClient bricklinkAjaxClient() {
        return Feign
                .builder()
                .client(new OkHttpClient())
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .logger(new Slf4jLogger(BricklinkAjaxClient.class))
                .logLevel(feign.Logger.Level.FULL)
                .target(BricklinkAjaxClient.class, "https://www.bricklink.com");
    }
}
