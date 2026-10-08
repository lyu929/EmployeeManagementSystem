package io.github.lyu929.ems.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class EnvironmentSafetyTest {

    @Test
    void isolatedDevelopmentAndTestProfilesKeepWorking() {
        for (String profile : new String[] {"dev", "test"}) {
            assertThatCode(() -> new EnvironmentSafety(properties(true, "dev-only-jwt", "test-only-aes",
                    "test-only-hmac"), environment(profile))).doesNotThrowAnyException();
        }
    }

    @Test
    void productionCannotAlsoActivateALocalProfile() {
        for (String local : new String[] {"dev", "test"}) {
            MockEnvironment environment = new MockEnvironment();
            environment.setActiveProfiles("prod", local);
            assertThatThrownBy(() -> new EnvironmentSafety(properties(false, "jwt", "aes", "hmac"), environment))
                    .isInstanceOf(IllegalStateException.class).hasMessageContaining("cannot be combined");
        }
    }

    @Test
    void productionAndStagingRejectDemoData() {
        for (String profile : new String[] {"prod", "staging"}) {
            assertThatThrownBy(() -> new EnvironmentSafety(properties(true, "jwt", "aes", "hmac"),
                    environment(profile))).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Demo data");
        }
    }

    @Test
    void publicKeysAreRejectedInEverySecretPosition() {
        for (String prefix : new String[] {"dev-only-", "test-only-"}) {
            for (int position = 0; position < 3; position++) {
                String[] keys = {"private-jwt", "private-aes", "private-hmac"};
                keys[position] = prefix + "public-course-key";
                AppProperties properties = properties(false, keys[0], keys[1], keys[2]);
                assertThatThrownBy(() -> new EnvironmentSafety(properties, environment("prod")))
                        .isInstanceOf(IllegalStateException.class).hasMessageContaining("public development or test key");
            }
        }
    }

    @Test
    void separateProductionSecretsAreAccepted() {
        assertThatCode(() -> new EnvironmentSafety(properties(false, "private-jwt", "private-aes", "private-hmac"),
                environment("prod"))).doesNotThrowAnyException();
    }

    private static MockEnvironment environment(String profile) {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles(profile);
        return environment;
    }

    private static AppProperties properties(boolean demo, String jwt, String aes, String hmac) {
        return new AppProperties(new AppProperties.Security(Duration.ofMinutes(30), 5, Duration.ofMinutes(15),
                encoded(jwt), encoded(aes), encoded(hmac)), new AppProperties.DemoData(demo, null, null), null);
    }

    private static String encoded(String label) {
        return Base64.getEncoder().encodeToString(label.getBytes(StandardCharsets.US_ASCII));
    }
}
