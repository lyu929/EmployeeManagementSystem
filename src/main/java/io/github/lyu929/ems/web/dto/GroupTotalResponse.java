package io.github.lyu929.ems.web.dto;

import java.math.BigDecimal;

public record GroupTotalResponse(String group, long statements, BigDecimal totalPay) {}
