package io.github.lyu929.ems.web.dto;

/** The logged-in user and, for employees, their own record. */
public record MeResponse(String username, String role, EmployeeResponse employee) {}
