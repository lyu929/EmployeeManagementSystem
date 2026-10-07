package io.github.lyu929.ems.web.dto;

import io.github.lyu929.ems.domain.Role;

public record UserResponse(Long id, String username, Role role, Integer employeeId, boolean enabled) {}
