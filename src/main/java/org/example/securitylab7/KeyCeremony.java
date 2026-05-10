package org.example.securitylab7;

import com.codahale.shamir.Scheme;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Scanner;

public class KeyCeremony {

    private static final int THRESHOLD = 2;
    private static final int TOTAL_SHARES = 3;

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== KEY CEREMONY ===");
        System.out.println("This will generate a new master key and split it into shares.");
        System.out.printf("Threshold: %d of %d shares required to unseal.%n%n", THRESHOLD, TOTAL_SHARES);
        System.out.print("Type YES to continue: ");

        String confirm = scanner.nextLine();
        if (!confirm.equals("YES")) {
            System.out.println("Aborted.");
            return;
        }

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256, SecureRandom.getInstanceStrong());
        SecretKey masterKey = keyGen.generateKey();
        byte[] keyBytes = masterKey.getEncoded();

        Scheme sharing = new Scheme(new SecureRandom(), TOTAL_SHARES, THRESHOLD);
        Map<Integer, byte[]> shares = sharing.split(keyBytes);

        System.out.println("\n=== SHARES ===");
        System.out.println("Distribute each share to its holder NOW.");
        System.out.println("This screen will not be shown again.\n");

        shares.forEach((index, shareBytes) ->
                System.out.printf("Share %d: %s%n",
                        index,
                        Base64.getEncoder().encodeToString(shareBytes))
        );

        Arrays.fill(keyBytes, (byte) 0);

        System.out.println("\n=== DONE ===");
        System.out.println("Master key has been split and cleared from memory.");
        System.out.println("Store each share securely. Never store them together.");
    }
}
