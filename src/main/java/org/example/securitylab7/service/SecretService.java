package org.example.securitylab7.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.securitylab7.repository.AuditLogRepository;
import org.example.securitylab7.repository.SecretRepository;
import org.example.securitylab7.dto.SecretRequest;
import org.example.securitylab7.dto.SecretResponse;
import org.example.securitylab7.dto.SecretValueResponse;
import org.example.securitylab7.entity.AuditLog;
import org.example.securitylab7.entity.Secret;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SecretService {

    private final SecretRepository secretRepository;
    private final AuditLogRepository auditLogRepository;
    private final CryptoService cryptoService;
    private final KeyService keyService;

    public SecretResponse create(SecretRequest request) {
        if (secretRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Secret with this name already exists");
        }

        try {
            SecretKey kek = keyService.getMasterKey();
            SecretKey dek = cryptoService.generateKey();

            String encryptedValue = cryptoService.encrypt(request.getValue(), dek);
            String encryptedDek = cryptoService.encrypt(cryptoService.keyToBase64(dek), kek);

            Secret entity = new Secret();
            entity.setName(request.getName());
            entity.setEncryptedValue(encryptedValue);
            entity.setEncryptedDek(encryptedDek);

            Secret saved = secretRepository.save(entity);

            audit(saved.getId(), saved.getName(), "CREATE", null);

            return toResponse(saved);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create secret", e);
        }
    }

    @Transactional()
    public List<SecretResponse> list() {
        return secretRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional()
    public SecretValueResponse reveal(UUID id) {
        Secret entity = findById(id);

        try {
            SecretKey kek = keyService.getMasterKey();

            String dekBase64 = cryptoService.decrypt(entity.getEncryptedDek(), kek);
            SecretKey dek = cryptoService.base64ToKey(dekBase64);

            String plainValue = cryptoService.decrypt(entity.getEncryptedValue(), dek);

            audit(entity.getId(), entity.getName(), "REVEAL", null);

            return SecretValueResponse.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .value(plainValue)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt secret", e);
        }
    }

    private Secret findById(UUID id) {
        return secretRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Secret not found: " + id));
    }

    private void audit(UUID secretId, String secretName, String action, UUID tokenId) {
        AuditLog log = new AuditLog();
        log.setSecretId(secretId);
        log.setSecretName(secretName);
        log.setAction(action);
        log.setTokenId(tokenId);
        auditLogRepository.save(log);
    }

    private SecretResponse toResponse(Secret entity) {
        return SecretResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }
}
