package io.github.lyu929.ems.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PayrollSummaryResponse(LocalDate payDate, long statements, BigDecimal totalEarnings,
        BigDecimal totalDeductions, BigDecimal netTotal) {}
