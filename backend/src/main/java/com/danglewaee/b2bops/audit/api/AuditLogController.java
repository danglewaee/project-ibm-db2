package com.danglewaee.b2bops.audit.api;

import com.danglewaee.b2bops.audit.application.AuditLogService;
import com.danglewaee.b2bops.audit.application.dto.AuditLogSummary;
import com.danglewaee.b2bops.audit.domain.AuditEntityType;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<AuditLogResponse> getLogs(
            @RequestParam(required = false) String correlationId,
            @RequestParam(required = false) AuditEntityType entityType,
            @RequestParam(required = false) Long entityId
    ) {
        List<AuditLogSummary> entries;
        if (correlationId != null && !correlationId.isBlank()) {
            entries = auditLogService.getByCorrelationId(correlationId);
        } else if (entityType != null && entityId != null) {
            entries = auditLogService.getByEntity(entityType, entityId);
        } else {
            throw new IllegalArgumentException(
                    "Provide correlationId or entityType + entityId to query audit logs"
            );
        }

        return ResponseEntity.ok(new AuditLogResponse(
                entries.stream()
                        .map(entry -> new AuditLogResponse.Entry(
                                entry.auditId(),
                                entry.entityType(),
                                entry.entityId(),
                                entry.action(),
                                entry.actor(),
                                entry.correlationId(),
                                entry.createdAt(),
                                entry.beforeState(),
                                entry.afterState()
                        ))
                        .toList()
        ));
    }
}
