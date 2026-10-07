package io.github.lyu929.ems.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class SsnProtectorTest {

    private final SsnProtector protector = new SsnProtector(
            "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.US_ASCII),
            "fedcba9876543210fedcba9876543210".getBytes(StandardCharsets.US_ASCII));

    @Test
    void encryptionRoundTripsAndIsRandomised() {
        String a = protector.encrypt("123-45-6789");
        String b = protector.encrypt("123456789");
        assertThat(a).isNotEqualTo(b).doesNotContain("6789");
        assertThat(protector.decrypt(a)).isEqualTo("123-45-6789");
        assertThat(protector.decrypt(b)).isEqualTo("123-45-6789");
    }

    @Test
    void tamperedCiphertextIsRejected() {
        byte[] raw = Base64.getDecoder().decode(protector.encrypt("123-45-6789"));
        raw[raw.length - 1] ^= 1;
        assertThatThrownBy(() -> protector.decrypt(Base64.getEncoder().encodeToString(raw)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void blindIndexIsDeterministicFormatInsensitiveAndKeyed() {
        assertThat(protector.blindIndex("123-45-6789")).isEqualTo(protector.blindIndex("123 45 6789")).hasSize(64);
        SsnProtector other = new SsnProtector("another-aes-key-of-32-bytes!!!!!".getBytes(StandardCharsets.US_ASCII),
                "another-hmac-key-of-32-bytes!!!!".getBytes(StandardCharsets.US_ASCII));
        assertThat(other.blindIndex("123-45-6789")).isNotEqualTo(protector.blindIndex("123-45-6789"));
    }

    @Test
    void maskingAndValidation() {
        assertThat(SsnProtector.mask(SsnProtector.last4("123-45-6789"))).isEqualTo("***-**-6789");
        assertThat(SsnProtector.mask(null)).isNull();
        assertThatThrownBy(() -> SsnProtector.normalize("12-345")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SsnProtector.normalize("abc-de-fghi")).isInstanceOf(IllegalArgumentException.class);
        byte[] same = "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.US_ASCII);
        assertThatThrownBy(() -> new SsnProtector(same, same)).isInstanceOf(IllegalStateException.class);
    }
}
