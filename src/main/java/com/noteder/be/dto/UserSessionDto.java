package com.noteder.be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSessionDto {
    private UUID id;
    private UUID userId;
    private String userAgent;
    private String ipAddress;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
