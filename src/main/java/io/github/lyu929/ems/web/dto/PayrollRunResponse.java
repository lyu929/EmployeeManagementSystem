package io.github.lyu929.ems.web.dto;

import java.time.LocalDate;

/** {@code skipped}: employees who already had a statement for that date. */
public record PayrollRunResponse(LocalDate payDate, int created, int skipped) {}
