package com.danglewaee.b2bops.audit.application;

import com.danglewaee.b2bops.audit.application.dto.AuditLogSummary;
import com.danglewaee.b2bops.audit.domain.AuditAction;
import com.danglewaee.b2bops.audit.domain.AuditEntityType;
import java.util.List;

public interface AuditLogService {

    void record(
            AuditEntityType entityType,
            Long entityId,
            AuditAction action,
            Object beforeState,
            Object afterState,
            String actor,
            String correlationId
    );

    List<AuditLogSummary> getByCorrelationId(String correlationId);

    List<AuditLogSummary> getByEntity(AuditEntityType entityType, Long entityId);
}
