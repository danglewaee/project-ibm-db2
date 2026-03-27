package com.danglewaee.b2bops.audit.api;

import com.danglewaee.b2bops.audit.domain.AuditAction;
import com.danglewaee.b2bops.audit.domain.AuditEntityType;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.List;

public record AuditLogResponse(
        List<Entry> entries
) {
    public record Entry(
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
}
