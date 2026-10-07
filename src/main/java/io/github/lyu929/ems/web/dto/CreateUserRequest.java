package io.github.lyu929.ems.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.lyu929.ems.domain.Role;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Size(min = 3, max = 50) @Pattern(regexp = "[A-Za-z0-9._-]+") String username,
        @NotBlank @Size(min = 10, max = 72, message = "must be 10 to 72 characters") String password,
        @NotNull Role role,
        Integer employeeId) {

    @JsonIgnore
    @AssertTrue(message = "an EMPLOYEE login must be linked to an employeeId")
    public boolean isLinkedWhenEmployee() {
        return role != Role.EMPLOYEE || employeeId != null;
    }
}
