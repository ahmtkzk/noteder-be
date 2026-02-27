package com.noteder.be.controller;

import com.noteder.be.dto.RefreshTokenDto;
import com.noteder.be.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/v1/refresh-tokens")
@RequiredArgsConstructor
public class RefreshTokenController {

    private final RefreshTokenService refreshTokenService;

    @PostMapping
    public ResponseEntity<RefreshTokenDto> createRefreshToken(@RequestParam UUID userId, @RequestParam String tokenHash, @RequestParam LocalDateTime expiresAt) {
        return ResponseEntity.ok(refreshTokenService.createRefreshToken(userId, tokenHash, expiresAt));
    }

    @GetMapping("/{tokenHash}")
    public ResponseEntity<RefreshTokenDto> getByTokenHash(@PathVariable String tokenHash) {
        return ResponseEntity.ok(refreshTokenService.getByTokenHash(tokenHash));
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteByUserId(@PathVariable UUID userId) {
        refreshTokenService.deleteByUserId(userId);
        return ResponseEntity.noContent().build();
    }
}
