package org.example.securitylab7;

public class VaultSealedException extends RuntimeException {
  public VaultSealedException(String message) {
    super(message);
  }
}
