package io.github.lyu929.ems.web.dto;

import java.math.BigDecimal;
import java.util.List;

/** Earnings for one calendar month, grouped by job title or division. */
public record PeriodReportResponse(String month, String groupedBy, List<GroupTotalResponse> rows,
        BigDecimal grandTotal) {}
