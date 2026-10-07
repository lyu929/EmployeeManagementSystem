package io.github.lyu929.ems.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PayStatementResponse(
        Long id,
        Integer employeeId,
        LocalDate payDate,
        BigDecimal earnings,
        BigDecimal fedTax,
        BigDecimal fedMed,
        BigDecimal fedSs,
        BigDecimal stateTax,
        BigDecimal retire401k,
        BigDecimal healthCare,
        BigDecimal totalDeductions,
        BigDecimal netPay) {}
