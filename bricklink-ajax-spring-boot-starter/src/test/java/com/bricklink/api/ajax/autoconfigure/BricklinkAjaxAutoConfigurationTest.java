package com.bricklink.api.ajax.autoconfigure;

import com.bricklink.api.ajax.BricklinkAjaxClient;
import com.bricklink.api.ajax.client.BricklinkAjaxHttpClient;
import com.bricklink.api.ajax.client.RateLimitedBricklinkAjaxHttpClient;
import com.bricklink.api.ajax.ratelimit.BricklinkAjaxRateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class BricklinkAjaxAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(BricklinkAjaxAutoConfiguration.class));

    @Test
    void createsAjaxClientWithRateLimiterByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(BricklinkAjaxClient.class);
            assertThat(context).hasSingleBean(BricklinkAjaxRateLimiter.class);
            assertThat(context.getBean("bricklinkAjaxHttpClient", BricklinkAjaxHttpClient.class))
                    .isInstanceOf(RateLimitedBricklinkAjaxHttpClient.class);
        });
    }

    @Test
    void canDisableRateLimiter() {
        contextRunner.withPropertyValues("bricklink.ajax.rate-limit.enabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(BricklinkAjaxClient.class);
                    assertThat(context.getBean("bricklinkAjaxHttpClient", BricklinkAjaxHttpClient.class))
                            .isNotInstanceOf(RateLimitedBricklinkAjaxHttpClient.class);
                });
    }
}
