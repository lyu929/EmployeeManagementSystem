package io.github.lyu929.ems.web.dto;

import java.math.BigDecimal;

public record SalaryChange(Integer employeeId, String name, BigDecimal oldSalary, BigDecimal newSalary) {}
