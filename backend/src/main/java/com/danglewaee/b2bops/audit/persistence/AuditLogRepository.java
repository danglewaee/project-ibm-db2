package com.danglewaee.b2bops.audit.persistence;

import com.danglewaee.b2bops.audit.domain.AuditEntityType;
import com.danglewaee.b2bops.audit.domain.AuditLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByCorrelationIdOrderByCreatedAtAsc(String correlationId);

    List<AuditLog> findAllByEntityTypeAndEntityIdOrderByCreatedAtAsc(
            AuditEntityType entityType,
            Long entityId
    );
}
