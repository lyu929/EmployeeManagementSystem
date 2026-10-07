package io.github.lyu929.ems.web.dto;

import java.time.LocalDate;

public record NewHireResponse(Integer employeeId, String firstName, String lastName, LocalDate hireDate,
        String jobTitle) {}
