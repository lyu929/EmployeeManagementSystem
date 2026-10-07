package io.github.lyu929.ems.security;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.lyu929.ems.config.AppProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class LoginAttemptServiceTest {

    /** A clock the test can move forward. */
    static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-01-01T00:00:00Z");

        void advance(Duration d) {
            now = now.plus(d);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private final MutableClock clock = new MutableClock();
    private final LoginAttemptService service = new LoginAttemptService(new AppProperties(
            new AppProperties.Security(Duration.ofMinutes(30), 3, Duration.ofMinutes(15), null, null, null),
            null, null), clock);

    @Test
    void locksAfterMaxFailuresAndUnlocksLater() {
        service.recordFailure("Alice");
        service.recordFailure("alice ");
        assertThat(service.lockedFor("alice")).isEmpty();
        service.recordFailure("ALICE");
        assertThat(service.lockedFor("alice")).contains(Duration.ofMinutes(15));
        clock.advance(Duration.ofMinutes(10));
        assertThat(service.lockedFor("alice")).contains(Duration.ofMinutes(5));
        clock.advance(Duration.ofMinutes(6));
        assertThat(service.lockedFor("alice")).isEmpty();
    }

    @Test
    void successResetsAndOldFailuresExpire() {
        service.recordFailure("bob");
        service.recordFailure("bob");
        service.recordSuccess("bob");
        service.recordFailure("bob");
        assertThat(service.lockedFor("bob")).isEmpty();
        service.recordFailure("carol");
        service.recordFailure("carol");
        clock.advance(Duration.ofMinutes(20)); // outside the counting window
        service.recordFailure("carol");
        assertThat(service.lockedFor("carol")).isEmpty();
    }
}
