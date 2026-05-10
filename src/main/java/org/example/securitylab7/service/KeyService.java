package org.example.securitylab7.service;

import com.codahale.shamir.Scheme;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.securitylab7.dto.UnsealStatus;
import org.example.securitylab7.VaultSealedException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KeyService {
    private volatile SecretKey masterKey;
    @Getter
    private volatile boolean sealed = true;
    private final Map<Integer, byte[]> collectedShares = new ConcurrentHashMap<>();
    private final CryptoService cryptoService;

    public static final int THRESHOLD = 2;
    private static final int TOTAL_SHARES = 3;

    @PostConstruct
    public void init() {
        String masterKeyBase64 = System.getenv("MASTER_KEY");
        if (masterKeyBase64 != null && !masterKeyBase64.isBlank()) {
            this.masterKey = cryptoService.base64ToKey(masterKeyBase64);
            this.sealed = false;
        }
    }

    public UnsealStatus provideShare(int shareIndex, String shareBase64) {
        if (!sealed) {
            return UnsealStatus.already("Vault is already unsealed");
        }

        collectedShares.put(shareIndex, Base64.getDecoder().decode(shareBase64));

        if (collectedShares.size() >= THRESHOLD) {
            tryUnseal();
        }

        return UnsealStatus.progress(
                collectedShares.size(),
                THRESHOLD,
                Math.max(0, THRESHOLD - collectedShares.size()),
                !sealed
        );
    }

    private void tryUnseal() {
        try {
            Map<Integer, byte[]> sharesToUse = collectedShares.entrySet().stream()
                    .limit(THRESHOLD)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            Scheme sharing = new Scheme(new SecureRandom(), TOTAL_SHARES, THRESHOLD);
            byte[] recoveredKeyBytes = sharing.join(sharesToUse);
            SecretKey recoveredKey = new SecretKeySpec(recoveredKeyBytes, "AES");

            validateKey(recoveredKey);

            this.masterKey = recoveredKey;
            this.sealed = false;
            collectedShares.clear();
            Arrays.fill(recoveredKeyBytes, (byte) 0);

        } catch (Exception e) {
            collectedShares.clear();
            throw new IllegalArgumentException("Failed to recover master key: " + e.getMessage());
        }
    }

    private void validateKey(SecretKey key) {
        try {
            String test = "validation-test";
            String encrypted = cryptoService.encrypt(test, key);
            String decrypted = cryptoService.decrypt(encrypted, key);
            if (!test.equals(decrypted)) {
                throw new IllegalArgumentException("Key validation failed");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid master key: " + e.getMessage());
        }
    }

    public void seal() {
        this.masterKey = null;
        this.sealed = true;
        this.collectedShares.clear();
    }

    public SecretKey getMasterKey() {
        if (sealed || masterKey == null) {
            throw new VaultSealedException("Vault is sealed");
        }
        return masterKey;
    }

    public UnsealStatus getStatus() {
        return UnsealStatus.progress(
                collectedShares.size(),
                THRESHOLD,
                Math.max(0, THRESHOLD - collectedShares.size()),
                !sealed
        );
    }
}
