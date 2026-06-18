package com.bricklink.api.ajax.ratelimit;

import java.time.Duration;

public class ThreadSleeper implements Sleeper {
    public static final ThreadSleeper INSTANCE = new ThreadSleeper();

    private ThreadSleeper() {
    }

    @Override
    public void sleep(Duration duration) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            return;
        }
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for BrickLink AJAX rate limiter", e);
        }
    }
}
