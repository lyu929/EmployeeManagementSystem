package io.github.lyu929.ems.web.dto;

/** Bearer token returned by the login endpoint. */
public record TokenResponse(String accessToken, String tokenType, long expiresIn, String role, String username) {}
