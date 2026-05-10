package org.example.securitylab7.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class SecretValueResponse {
    private UUID id;
    private String name;
    private String value;
}