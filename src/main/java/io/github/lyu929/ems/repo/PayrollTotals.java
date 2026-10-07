package io.github.lyu929.ems.repo;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Aggregate of all pay statements for one pay date (JPQL constructor projection). */
public record PayrollTotals(LocalDate payDate, Long statements, BigDecimal earnings, BigDecimal deductions) {}
