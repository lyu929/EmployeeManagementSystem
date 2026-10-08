package io.github.lyu929.ems.config;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

/** Keeps the public course credentials and keys out of production configurations. */
@Component
public class EnvironmentSafety {

    public EnvironmentSafety(AppProperties properties, Environment environment) {
        boolean local = environment.acceptsProfiles(Profiles.of("dev", "test"));
        boolean production = environment.acceptsProfiles(Profiles.of("prod"));
        if (production && local) {
            throw new IllegalStateException("The prod profile cannot be combined with dev or test");
        }
        if (local) {
            return;
        }
        if (properties.demoData() != null && properties.demoData().enabled()) {
            throw new IllegalStateException("Demo data is only allowed in the dev and test profiles");
        }
        AppProperties.Security security = properties.security();
        rejectDemoKey(security.jwtSecret(), "app.security.jwt-secret");
        rejectDemoKey(security.ssnEncryptionKey(), "app.security.ssn-encryption-key");
        rejectDemoKey(security.ssnHmacKey(), "app.security.ssn-hmac-key");
    }

    private static void rejectDemoKey(String base64, String name) {
        if (base64 == null || base64.isBlank()) {
            return; // KeyMaterial provides the existing missing/invalid-key checks.
        }
        byte[] key;
        try {
            key = Base64.getDecoder().decode(base64.trim());
        } catch (IllegalArgumentException e) {
            return; // Leave the invalid-base64 error to KeyMaterial as well.
        }
        String label = new String(key, StandardCharsets.US_ASCII);
        if (label.startsWith("dev-only-") || label.startsWith("test-only-")) {
            throw new IllegalStateException(name + " must not use a public development or test key");
        }
    }
}
