package io.github.lyu929.ems.security;

import io.github.lyu929.ems.config.AppProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Temporary lockout after repeated failed logins for the same username (brute-force protection).
 * In-memory, which is fine for a single instance; a multi-instance deployment would use a shared store.
 */
@Component
public class LoginAttemptService {

    private record Attempts(int failures, Instant firstFailure, Instant lockedUntil) {}

    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();
    private final int maxAttempts;
    private final Duration lockout;
    private final Clock clock;

    public LoginAttemptService(AppProperties properties, Clock clock) {
        this.maxAttempts = Math.max(1, properties.security().maxLoginAttempts());
        this.lockout = properties.security().loginLockout();
        this.clock = clock;
    }

    /** Time left until the username may try again, if it is currently locked. */
    public Optional<Duration> lockedFor(String username) {
        Attempts a = attempts.get(key(username));
        Instant now = clock.instant();
        if (a == null || a.lockedUntil() == null || !a.lockedUntil().isAfter(now)) {
            return Optional.empty();
        }
        return Optional.of(Duration.between(now, a.lockedUntil()));
    }

    public void recordFailure(String username) {
        Instant now = clock.instant();
        attempts.compute(key(username), (k, a) -> {
            if (a == null || now.isAfter(a.firstFailure().plus(lockout))) {
                a = new Attempts(0, now, null); // the counting window restarts
            }
            int failures = a.failures() + 1;
            Instant lockedUntil = failures >= maxAttempts ? now.plus(lockout) : a.lockedUntil();
            return new Attempts(failures, a.firstFailure(), lockedUntil);
        });
    }

    public void recordSuccess(String username) {
        attempts.remove(key(username));
    }

    private static String key(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}
