package org.example.securitylab7.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SecretRequest {
    private String name;
    private String value;
}
