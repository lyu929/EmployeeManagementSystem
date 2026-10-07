package io.github.lyu929.ems.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** Generate pay statements for every employee for one pay date (idempotent). */
public record PayrollRunRequest(@NotNull LocalDate payDate) {}
