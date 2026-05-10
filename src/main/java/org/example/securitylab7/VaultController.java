package org.example.securitylab7;

import lombok.RequiredArgsConstructor;
import org.example.securitylab7.dto.ShareRequest;
import org.example.securitylab7.dto.UnsealStatus;
import org.example.securitylab7.service.KeyService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/vault")
@RequiredArgsConstructor
public class VaultController {

    private final KeyService keyService;

    @GetMapping("/status")
    public UnsealStatus status() {
        return keyService.getStatus();
    }

    @PostMapping("/unseal")
    public UnsealStatus unseal(@RequestBody ShareRequest request) {
        return keyService.provideShare(request.getShareIndex(), request.getShare());
    }

    @PostMapping("/seal")
    public Map<String, String> seal() {
        keyService.seal();
        return Map.of("status", "sealed");
    }
}
