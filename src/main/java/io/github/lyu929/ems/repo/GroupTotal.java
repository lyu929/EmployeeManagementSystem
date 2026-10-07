package io.github.lyu929.ems.repo;

import java.math.BigDecimal;

/** Earnings summed per group (job title or division); {@code groupName} is null for unassigned employees. */
public record GroupTotal(String groupName, Long statements, BigDecimal earnings) {}
