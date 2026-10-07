package io.github.lyu929.ems.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** SSN search goes in a POST body so the number never appears in URLs, proxies or access logs. */
public record SsnSearchRequest(
        @NotBlank @Pattern(regexp = "\\d{3}-?\\d{2}-?\\d{4}", message = "must look like 123-45-6789") String ssn) {}
