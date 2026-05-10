package org.example.securitylab7;

import org.example.securitylab7.dto.UnsealStatus;
import org.example.securitylab7.service.KeyService;
import org.springframework.stereotype.Component;

import java.io.Console;
import java.util.Arrays;
import java.util.Scanner;

@Component
public class UnsealCLI {

    private final KeyService keyService;

    public UnsealCLI(KeyService keyService) {
        this.keyService = keyService;
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== VAULT UNSEAL ===");
        System.out.printf("Required: %d shares%n%n", KeyService.THRESHOLD);

        while (keyService.isSealed()) {
            try {
                System.out.print("Enter share index: ");
                int index = Integer.parseInt(scanner.nextLine().trim());

                System.out.print("Enter share: ");
                String share = scanner.nextLine().trim();

                UnsealStatus status = keyService.provideShare(index, share);
                System.out.println(status.getMessage() + "\n");

            } catch (NumberFormatException e) {
                System.out.println("Invalid index, try again.");
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid share: " + e.getMessage());
            }
        }

        System.out.println("Vault unsealed. Starting service...");
    }
}
