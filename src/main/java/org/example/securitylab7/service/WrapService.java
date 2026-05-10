package org.example.securitylab7.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.securitylab7.dto.UnwrapResponse;
import org.example.securitylab7.dto.WrapResponse;
import org.example.securitylab7.entity.Secret;
import org.example.securitylab7.entity.WrapToken;
import org.example.securitylab7.repository.SecretRepository;
import org.example.securitylab7.repository.WrapTokenRepository;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class WrapService {

    private final WrapTokenRepository wrapTokenRepository;
    private final SecretRepository secretRepository;
    private final CryptoService cryptoService;
    private final KeyService keyService;

    public WrapResponse wrap(UUID secretId, int ttlSeconds) {
        Secret secret = secretRepository.findById(secretId)
                .orElseThrow(() -> new NoSuchElementException("Secret not found: " + secretId));

        try {
            SecretKey kek = keyService.getMasterKey();

            String dekBase64 = cryptoService.decrypt(secret.getEncryptedDek(), kek);
            SecretKey originalDek = cryptoService.base64ToKey(dekBase64);
            String plainValue = cryptoService.decrypt(secret.getEncryptedValue(), originalDek);

            SecretKey wrapDek = cryptoService.generateKey();
            String encryptedValue = cryptoService.encrypt(plainValue, wrapDek);
            String encryptedDek = cryptoService.encrypt(cryptoService.keyToBase64(wrapDek), kek);

            String token = generateToken();

            WrapToken entity = new WrapToken();
            entity.setToken(token);
            entity.setSecretId(secretId);
            entity.setEncryptedValue(encryptedValue);
            entity.setEncryptedDek(encryptedDek);
            entity.setExpiresAt(LocalDateTime.now().plusSeconds(ttlSeconds));
            entity.setUsed(false);

            wrapTokenRepository.save(entity);

            return new WrapResponse(token, entity.getExpiresAt());

        } catch (Exception e) {
            throw new RuntimeException("Failed to wrap secret", e);
        }
    }

    public UnwrapResponse unwrap(String token) {
        WrapToken entity = wrapTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (LocalDateTime.now().isAfter(entity.getExpiresAt())) {
            throw new IllegalStateException("Token has expired");
        }

        if (entity.isUsed()) {
            throw new IllegalStateException("Token has already been used");
        }

        try {
            SecretKey kek = keyService.getMasterKey();

            String dekBase64 = cryptoService.decrypt(entity.getEncryptedDek(), kek);
            SecretKey dek = cryptoService.base64ToKey(dekBase64);
            String plainValue = cryptoService.decrypt(entity.getEncryptedValue(), dek);

            entity.setUsed(true);
            wrapTokenRepository.save(entity);

            String secretName = secretRepository.findById(entity.getSecretId())
                    .map(Secret::getName)
                    .orElse("unknown");

            return new UnwrapResponse(secretName, plainValue);

        } catch (Exception e) {
            throw new RuntimeException("Failed to unwrap secret", e);
        }
    }

    private String generateToken() {
        byte[] tokenBytes = new byte[32];
        new SecureRandom().nextBytes(tokenBytes);
        return "wrap_" + Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}