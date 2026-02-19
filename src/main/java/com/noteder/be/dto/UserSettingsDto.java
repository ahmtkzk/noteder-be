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
public class UserSettingsDto {
    private UUID userId;
    private String theme;
    private String colorTheme;
    private String fontSize;
    private String defaultCategory;
    private String defaultNoteColor;
    private String defaultSecurePassword;
    private boolean showStats;
    private LocalDateTime updatedAt;
}
