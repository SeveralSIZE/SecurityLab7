package org.example.securitylab7.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wrap_tokens")
@Data
public class WrapToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "secret_id", nullable = false)
    private UUID secretId;

    @Column(name = "encrypted_value", nullable = false)
    private String encryptedValue;

    @Column(name = "encrypted_dek", nullable = false)
    private String encryptedDek;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
