package org.example.securitylab7;


import org.example.securitylab7.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findBySecretIdOrderByPerformedAtDesc(UUID secretId);
}
