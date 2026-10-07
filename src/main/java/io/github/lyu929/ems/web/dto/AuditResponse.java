package io.github.lyu929.ems.web.dto;

import java.time.Instant;

public record AuditResponse(Long id, Instant occurredAt, String actor, String action, String entityType,
        String entityId, String details) {}
