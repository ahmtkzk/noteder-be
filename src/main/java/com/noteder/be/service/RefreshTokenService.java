package com.noteder.be.service;

import com.noteder.be.dto.RefreshTokenDto;
import com.noteder.be.entity.RefreshToken;
import com.noteder.be.entity.User;
import com.noteder.be.repository.RefreshTokenRepository;
import com.noteder.be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public RefreshTokenDto createRefreshToken(UUID userId, String tokenHash, LocalDateTime expiresAt) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .build();

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        return mapToDto(savedToken);
    }

    public RefreshTokenDto getByTokenHash(String tokenHash) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
        return mapToDto(refreshToken);
    }

    @Transactional
    public void deleteByUserId(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    private RefreshTokenDto mapToDto(RefreshToken refreshToken) {
        return RefreshTokenDto.builder()
                .id(refreshToken.getId())
                .userId(refreshToken.getUser().getId())
                .tokenHash(refreshToken.getTokenHash())
                .expiresAt(refreshToken.getExpiresAt())
                .createdAt(refreshToken.getCreatedAt())
                .build();
    }
}
