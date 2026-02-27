package com.noteder.be.controller;

import com.noteder.be.dto.UserSettingsDto;
import com.noteder.be.service.UserSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/user-settings")
@RequiredArgsConstructor
public class UserSettingsController {

    private final UserSettingsService userSettingsService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserSettingsDto> getSettingsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(userSettingsService.getSettingsByUserId(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserSettingsDto> updateSettings(@PathVariable UUID userId, @RequestBody UserSettingsDto settingsDto) {
        return ResponseEntity.ok(userSettingsService.updateSettings(userId, settingsDto));
    }
}
