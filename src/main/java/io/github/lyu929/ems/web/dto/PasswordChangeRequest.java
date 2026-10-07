package io.github.lyu929.ems.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(@NotBlank String currentPassword,
        @NotBlank @Size(min = 10, max = 72, message = "must be 10 to 72 characters") String newPassword) {}
