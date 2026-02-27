package com.noteder.be.controller;

import com.noteder.be.dto.UserSessionDto;
import com.noteder.be.service.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/user-sessions")
@RequiredArgsConstructor
public class UserSessionController {

    private final UserSessionService userSessionService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserSessionDto>> getSessionsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(userSessionService.getSessionsByUserId(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable UUID id) {
        userSessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteAllSessionsByUserId(@PathVariable UUID userId) {
        userSessionService.deleteAllSessionsByUserId(userId);
        return ResponseEntity.noContent().build();
    }
}
