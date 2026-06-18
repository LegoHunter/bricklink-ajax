package com.bricklink.api.ajax.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@Getter
@Setter
@ConfigurationProperties(prefix = "bricklink.ajax")
public class BricklinkAjaxProperties {
    private URI uri = URI.create("https://www.bricklink.com");
    private RateLimit rateLimit = new RateLimit();
    private HttpLogging httpLogging = new HttpLogging();

    @Getter
    @Setter
    public static class RateLimit {
        private boolean enabled = true;
        private long minimumDelayMs = 2000;
    }

    @Getter
    @Setter
    public static class HttpLogging {
        private boolean enabled = false;
    }
}
