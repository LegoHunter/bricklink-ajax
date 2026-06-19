package com.bricklink.api.ajax.ratelimit;

import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Slf4j
public class BricklinkAjaxRateLimiter {
    private final Object lock = new Object();
    private final Duration minimumDelay;
    private final Clock clock;
    private final Sleeper sleeper;
    private Instant nextAllowedAt = Instant.EPOCH;

    public BricklinkAjaxRateLimiter(Duration minimumDelay, Clock clock, Sleeper sleeper) {
        this.minimumDelay = Objects.requireNonNull(minimumDelay, "minimumDelay cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper cannot be null");
        if (minimumDelay.isNegative()) {
            throw new IllegalArgumentException("minimumDelay cannot be negative");
        }
    }

    public void acquire(String operation) {
        synchronized (lock) {
            Instant now = clock.instant();
            if (now.isBefore(nextAllowedAt)) {
                Duration wait = Duration.between(now, nextAllowedAt);
                log.debug("bricklink_ajax.rate_limit_wait operation={} waitMs={}", operation, wait.toMillis());
                sleeper.sleep(wait);
                now = clock.instant();
            }
            nextAllowedAt = now.plus(minimumDelay);
        }
    }
}
