package com.bricklink.api.ajax.ratelimit;

import java.time.Duration;

@FunctionalInterface
public interface Sleeper {
    void sleep(Duration duration);
}
