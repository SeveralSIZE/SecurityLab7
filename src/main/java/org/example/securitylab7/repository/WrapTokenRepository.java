package org.example.securitylab7.repository;

import org.example.securitylab7.entity.WrapToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WrapTokenRepository extends JpaRepository<WrapToken, UUID> {
    Optional<WrapToken> findByToken(String token);
}
