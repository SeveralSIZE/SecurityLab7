package org.example.securitylab7;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.example.securitylab7.dto.UnwrapRequest;
import org.example.securitylab7.dto.UnwrapResponse;
import org.example.securitylab7.dto.WrapResponse;
import org.example.securitylab7.service.WrapService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/secrets")
@RequiredArgsConstructor
public class WrapController {

    private final WrapService wrapService;

    @GetMapping("/{id}/wrap")
    public WrapResponse wrap(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "300") int ttl) {
        return wrapService.wrap(id, ttl);
    }

    @PostMapping("/unwrap")
    public UnwrapResponse unwrap(@RequestBody UnwrapRequest request) {
        return wrapService.unwrap(request.getToken());
    }
}
