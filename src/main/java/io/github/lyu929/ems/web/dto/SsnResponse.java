package io.github.lyu929.ems.web.dto;

/** The unmasked SSN; every request for it is written to the audit log. */
public record SsnResponse(Integer employeeId, String ssn) {}
