package io.github.lyu929.ems.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Create / update an employee. On update a null {@code ssn} keeps the stored value.
 */
public record EmployeeRequest(
        @NotBlank @Size(max = 65) String firstName,
        @NotBlank @Size(max = 65) String lastName,
        @NotBlank @Email @Size(max = 120) String email,
        @NotNull @PastOrPresent LocalDate hireDate,
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal salary,
        @Pattern(regexp = "\\d{3}-?\\d{2}-?\\d{4}", message = "must look like 123-45-6789") String ssn,
        Integer divisionId,
        Integer jobTitleId) {}
