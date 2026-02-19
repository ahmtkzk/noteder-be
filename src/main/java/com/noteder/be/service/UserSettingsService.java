package com.noteder.be.service;

import com.noteder.be.dto.UserSettingsDto;
import com.noteder.be.entity.UserSettings;
import com.noteder.be.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserSettingsService {

    private final UserSettingsRepository userSettingsRepository;

    public UserSettingsDto getSettingsByUserId(UUID userId) {
        UserSettings settings = userSettingsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Settings not found for user"));
        return mapToDto(settings);
    }

    @Transactional
    public UserSettingsDto updateSettings(UUID userId, UserSettingsDto settingsDto) {
        UserSettings settings = userSettingsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Settings not found for user"));

        settings.setTheme(settingsDto.getTheme());
        settings.setColorTheme(settingsDto.getColorTheme());
        settings.setFontSize(settingsDto.getFontSize());
        settings.setDefaultCategory(settingsDto.getDefaultCategory());
        settings.setDefaultNoteColor(settingsDto.getDefaultNoteColor());
        settings.setDefaultSecurePassword(settingsDto.getDefaultSecurePassword());
        settings.setShowStats(settingsDto.isShowStats());

        UserSettings updatedSettings = userSettingsRepository.save(settings);
        return mapToDto(updatedSettings);
    }

    private UserSettingsDto mapToDto(UserSettings settings) {
        return UserSettingsDto.builder()
                .userId(settings.getUserId())
                .theme(settings.getTheme())
                .colorTheme(settings.getColorTheme())
                .fontSize(settings.getFontSize())
                .defaultCategory(settings.getDefaultCategory())
                .defaultNoteColor(settings.getDefaultNoteColor())
                .defaultSecurePassword(settings.getDefaultSecurePassword())
                .showStats(settings.isShowStats())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }
}
