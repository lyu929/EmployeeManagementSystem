package io.github.lyu929.ems.config;

import java.util.Base64;

/** Decodes and checks the base64 secrets from the configuration; fails fast when one is missing. */
public final class KeyMaterial {

    public static final int MIN_BYTES = 32;

    private KeyMaterial() {}

    public static byte[] decode(String base64, String name) {
        if (base64 == null || base64.isBlank()) {
            throw new IllegalStateException(name + " is not configured (base64, at least " + MIN_BYTES + " bytes)");
        }
        byte[] key;
        try {
            key = Base64.getDecoder().decode(base64.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(name + " is not valid base64", e);
        }
        if (key.length < MIN_BYTES) {
            throw new IllegalStateException(name + " must be at least " + MIN_BYTES + " bytes, got " + key.length);
        }
        return key;
    }
}
