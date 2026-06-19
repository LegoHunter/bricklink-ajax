package com.bricklink.api.ajax.client;

import com.bricklink.api.ajax.ratelimit.BricklinkAjaxRateLimiter;
import com.bricklink.api.ajax.ratelimit.Sleeper;
import com.bricklink.api.ajax.support.CatalogItemsForSaleResult;
import com.bricklink.api.ajax.support.SearchProductResult;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitedBricklinkAjaxHttpClientTest {
    @Test
    void protectsEveryAjaxHttpRequest() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-17T12:00:00Z"));
        RecordingSleeper sleeper = new RecordingSleeper(clock);
        BricklinkAjaxRateLimiter rateLimiter = new BricklinkAjaxRateLimiter(Duration.ofSeconds(2), clock, sleeper);
        RecordingHttpClient delegate = new RecordingHttpClient();
        RateLimitedBricklinkAjaxHttpClient client = new RateLimitedBricklinkAjaxHttpClient(delegate, rateLimiter);

        client.searchProduct(Map.of("q", "6390-1", "type", "S"));
        client.catalogItemsForSale(Map.of("itemid", 4997));
        client.searchProduct(Map.of("q", "6390-1", "type", "S"));

        assertThat(delegate.operations).containsExactly("search", "catalog", "search");
        assertThat(sleeper.sleeps).containsExactly(Duration.ofSeconds(2), Duration.ofSeconds(2));
    }

    private static class RecordingHttpClient implements BricklinkAjaxHttpClient {
        private final List<String> operations = new ArrayList<>();

        @Override
        public SearchProductResult searchProduct(Map<String, Object> params) {
            operations.add("search");
            return new SearchProductResult();
        }

        @Override
        public CatalogItemsForSaleResult catalogItemsForSale(Map<String, Object> params) {
            operations.add("catalog");
            return new CatalogItemsForSaleResult();
        }
    }

    private static class RecordingSleeper implements Sleeper {
        private final MutableClock clock;
        private final List<Duration> sleeps = new ArrayList<>();

        private RecordingSleeper(MutableClock clock) {
            this.clock = clock;
        }

        @Override
        public void sleep(Duration duration) {
            sleeps.add(duration);
            clock.advance(duration);
        }
    }

    private static class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }
    }
}
