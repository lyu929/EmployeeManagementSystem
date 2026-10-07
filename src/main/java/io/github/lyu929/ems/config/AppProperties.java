package io.github.lyu929.ems.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Typed view of the {@code app.*} configuration. */
@ConfigurationProperties(prefix = "app")
public record AppProperties(Security security, DemoData demoData, BootstrapAdmin bootstrapAdmin) {

    /**
     * @param tokenTtl lifetime of issued access tokens
     * @param maxLoginAttempts failed logins allowed per username before a temporary lockout
     * @param loginLockout how long a username stays locked
     * @param jwtSecret base64 HMAC key (>= 32 bytes) for signing access tokens
     * @param ssnEncryptionKey base64 AES-256 key for SSNs at rest
     * @param ssnHmacKey base64 key (>= 32 bytes) for the SSN blind index
     */
    public record Security(Duration tokenTtl, int maxLoginAttempts, Duration loginLockout, String jwtSecret,
            String ssnEncryptionKey, String ssnHmacKey) {}

    /** Seed the database with the course data set and demo logins (never in production). */
    public record DemoData(boolean enabled, String adminPassword, String employeePassword) {}

    /** Optional first HR admin created on start-up when no user exists yet. */
    public record BootstrapAdmin(String username, String password) {}
}
