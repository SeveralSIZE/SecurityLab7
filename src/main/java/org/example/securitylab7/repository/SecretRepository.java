package org.example.securitylab7;

import org.example.securitylab7.entity.AuditLog;
import org.example.securitylab7.entity.Secret;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SecretRepository extends JpaRepository<Secret, UUID> {
    Optional<Secret> findByName(String name);
    boolean existsByName(String name);
}