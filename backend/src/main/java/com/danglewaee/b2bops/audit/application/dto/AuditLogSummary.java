package com.danglewaee.b2bops.audit.application.dto;

import com.danglewaee.b2bops.audit.domain.AuditAction;
import com.danglewaee.b2bops.audit.domain.AuditEntityType;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public record AuditLogSummary(
        Long auditId,
        AuditEntityType entityType,
        Long entityId,
        AuditAction action,
        String actor,
        String correlationId,
        Instant createdAt,
        JsonNode beforeState,
        JsonNode afterState
) {
}
