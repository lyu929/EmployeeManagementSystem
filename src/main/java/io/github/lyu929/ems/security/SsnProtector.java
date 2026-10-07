package io.github.lyu929.ems.security;

import io.github.lyu929.ems.config.AppProperties;
import io.github.lyu929.ems.config.KeyMaterial;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

/**
 * Protects Social Security numbers at rest.
 *
 * <ul>
 *   <li>{@link #encrypt}: AES-256-GCM with a random 96-bit IV, stored as base64(iv || ciphertext || tag);
 *   <li>{@link #blindIndex}: HMAC-SHA256 of the normalised digits, so HR can still search by exact SSN
 *       without the database ever seeing the number;
 *   <li>{@link #mask}: "***-**-1234" for everything that is displayed.
 * </ul>
 */
@Component
public class SsnProtector {

    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final SecretKeySpec aesKey;
    private final SecretKeySpec hmacKey;
    private final SecureRandom random = new SecureRandom();

    public SsnProtector(AppProperties properties) {
        this(KeyMaterial.decode(properties.security().ssnEncryptionKey(), "app.security.ssn-encryption-key"),
                KeyMaterial.decode(properties.security().ssnHmacKey(), "app.security.ssn-hmac-key"));
    }

    SsnProtector(byte[] aesKey, byte[] hmacKey) {
        if (Arrays.equals(aesKey, hmacKey)) {
            throw new IllegalStateException("the SSN encryption key and HMAC key must differ");
        }
        this.aesKey = new SecretKeySpec(Arrays.copyOf(aesKey, 32), "AES");
        this.hmacKey = new SecretKeySpec(hmacKey, "HmacSHA256");
    }

    /** Digits only; throws if the value is not a 9-digit SSN (dashes and spaces are allowed). */
    public static String normalize(String ssn) {
        if (ssn == null) {
            throw new IllegalArgumentException("SSN is required");
        }
        String digits = ssn.replaceAll("[\\s-]", "");
        if (!digits.matches("\\d{9}")) {
            throw new IllegalArgumentException("SSN must have 9 digits");
        }
        return digits;
    }

    public static String format(String digits) {
        return digits.substring(0, 3) + "-" + digits.substring(3, 5) + "-" + digits.substring(5);
    }

    public static String last4(String ssn) {
        return normalize(ssn).substring(5);
    }

    public static String mask(String last4) {
        return last4 == null ? null : "***-**-" + last4;
    }

    public String encrypt(String ssn) {
        byte[] plain = normalize(ssn).getBytes(StandardCharsets.US_ASCII);
        try {
            byte[] iv = new byte[IV_BYTES];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(TAG_BITS, iv));
            byte[] sealed = cipher.doFinal(plain);
            return Base64.getEncoder().encodeToString(ByteBuffer.allocate(iv.length + sealed.length)
                    .put(iv).put(sealed).array());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("SSN encryption failed", e);
        }
    }

    /** Returns the SSN formatted as ddd-dd-dddd. Fails if the ciphertext was tampered with. */
    public String decrypt(String stored) {
        try {
            byte[] all = Base64.getDecoder().decode(stored);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(TAG_BITS, all, 0, IV_BYTES));
            byte[] plain = cipher.doFinal(all, IV_BYTES, all.length - IV_BYTES);
            return format(new String(plain, StandardCharsets.US_ASCII));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("SSN could not be decrypted", e);
        }
    }

    /** Deterministic keyed hash used as a unique, searchable index. */
    public String blindIndex(String ssn) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(hmacKey);
            return HexFormat.of().formatHex(mac.doFinal(normalize(ssn).getBytes(StandardCharsets.US_ASCII)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("SSN hashing failed", e);
        }
    }
}
