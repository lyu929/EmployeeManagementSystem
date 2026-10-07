package io.github.lyu929.ems.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** An employee as returned by the API. The SSN is always masked ("***-**-1234"). */
public record EmployeeResponse(
        Integer id,
        String firstName,
        String lastName,
        String email,
        LocalDate hireDate,
        BigDecimal salary,
        String ssn,
        Ref division,
        Ref jobTitle,
        long version) {}
