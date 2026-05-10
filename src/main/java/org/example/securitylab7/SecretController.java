package org.example.securitylab7;

import org.example.securitylab7.dto.SecretRequest;
import org.example.securitylab7.dto.SecretResponse;
import org.example.securitylab7.dto.SecretValueResponse;
import org.example.securitylab7.service.SecretService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/secrets")
public class SecretController {

    private final SecretService secretService;

    public SecretController(SecretService secretService) {
        this.secretService = secretService;
    }

    @PostMapping
    public ResponseEntity<SecretResponse> create(@RequestBody SecretRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(secretService.create(request));
    }

    @GetMapping
    public List<SecretResponse> list() {
        return secretService.list();
    }

    @GetMapping("/{id}/reveal")
    public SecretValueResponse reveal(
            @PathVariable UUID id) {
        return secretService.reveal(id);
    }
}
