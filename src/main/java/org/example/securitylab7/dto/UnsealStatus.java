package org.example.securitylab7.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UnsealStatus {

    private final int sharesProvided;
    private final int sharesRequired;
    private final int sharesRemaining;
    private final boolean unsealed;
    private final String message;

    public static UnsealStatus progress(int provided, int required, int remaining, boolean unsealed) {
        String msg = unsealed
                ? "Vault is unsealed and operational"
                : String.format("Need %d more share(s) to unseal", remaining);
        return new UnsealStatus(provided, required, remaining, unsealed, msg);
    }

    public static UnsealStatus already(String message) {
        return new UnsealStatus(0, 0, 0, true, message);
    }
}
