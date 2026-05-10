package org.example.securitylab7.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "secret_id")
    private UUID secretId;

    @Column(name = "secret_name")
    private String secretName;

    @Column(nullable = false)
    private String action;

    @Column(name = "token_id")
    private UUID tokenId;

    @Column(name = "performed_at")
    private LocalDateTime performedAt;

    @PrePersist
    public void prePersist() {
        performedAt = LocalDateTime.now();
    }
}
