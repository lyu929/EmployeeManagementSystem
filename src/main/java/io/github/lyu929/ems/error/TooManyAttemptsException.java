package io.github.lyu929.ems.error;

import java.time.Duration;

/** Too many failed logins for this username (HTTP 429 with Retry-After). */
public class TooManyAttemptsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Duration retryAfter;

    public TooManyAttemptsException(Duration retryAfter) {
        super("Too many failed login attempts; try again later");
        this.retryAfter = retryAfter;
    }

    public Duration getRetryAfter() {
        return retryAfter;
    }
}
