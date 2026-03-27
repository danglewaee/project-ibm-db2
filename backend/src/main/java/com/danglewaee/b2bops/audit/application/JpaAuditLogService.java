package com.danglewaee.b2bops.audit.application;

import com.danglewaee.b2bops.audit.application.dto.AuditLogSummary;
import com.danglewaee.b2bops.audit.domain.AuditAction;
import com.danglewaee.b2bops.audit.domain.AuditEntityType;
import com.danglewaee.b2bops.audit.domain.AuditLog;
import com.danglewaee.b2bops.audit.persistence.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class JpaAuditLogService implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public JpaAuditLogService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void record(
            AuditEntityType entityType,
            Long entityId,
            AuditAction action,
            Object beforeState,
            Object afterState,
            String actor,
            String correlationId
    ) {
        auditLogRepository.save(new AuditLog(
                entityType,
                entityId,
                action,
                serialize(beforeState),
                serialize(afterState),
                actor,
                correlationId
        ));
    }

    @Override
    public List<AuditLogSummary> getByCorrelationId(String correlationId) {
        return auditLogRepository.findAllByCorrelationIdOrderByCreatedAtAsc(correlationId).stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    public List<AuditLogSummary> getByEntity(AuditEntityType entityType, Long entityId) {
        return auditLogRepository.findAllByEntityTypeAndEntityIdOrderByCreatedAtAsc(entityType, entityId).stream()
                .map(this::toSummary)
                .toList();
    }

    private AuditLogSummary toSummary(AuditLog auditLog) {
        return new AuditLogSummary(
                auditLog.getId(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getAction(),
                auditLog.getActor(),
                auditLog.getCorrelationId(),
                auditLog.getCreatedAt(),
                parse(auditLog.getBeforeJson()),
                parse(auditLog.getAfterJson())
        );
    }

    private String serialize(Object state) {
        if (state == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize audit state", exception);
        }
    }

    private JsonNode parse(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to parse persisted audit JSON", exception);
        }
    }
}
