package io.github.lyu929.ems.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record SalaryAdjustmentResponse(BigDecimal percent, boolean applied, int affected, List<SalaryChange> changes) {}
