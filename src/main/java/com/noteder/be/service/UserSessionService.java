package com.noteder.be.service;

import com.noteder.be.dto.UserSessionDto;
import com.noteder.be.entity.User;
import com.noteder.be.entity.UserSession;
import com.noteder.be.repository.UserRepository;
import com.noteder.be.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final UserSessionRepository userSessionRepository;
    private final UserRepository userRepository;

    public List<UserSessionDto> getSessionsByUserId(UUID userId) {
        return userSessionRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createSession(UUID userId, String tokenHash, String userAgent, String ipAddress, LocalDateTime expiresAt) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserSession session = UserSession.builder()
                .user(user)
                .tokenHash(tokenHash)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .expiresAt(expiresAt)
                .build();

        userSessionRepository.save(session);
    }

    @Transactional
    public void deleteSession(UUID sessionId) {
        userSessionRepository.deleteById(sessionId);
    }

    @Transactional
    public void deleteAllSessionsByUserId(UUID userId) {
        userSessionRepository.deleteByUserId(userId);
    }

    private UserSessionDto mapToDto(UserSession session) {
        return UserSessionDto.builder()
                .id(session.getId())
                .userId(session.getUser().getId())
                .userAgent(session.getUserAgent())
                .ipAddress(session.getIpAddress())
                .expiresAt(session.getExpiresAt())
                .createdAt(session.getCreatedAt())
                .build();
    }
}
