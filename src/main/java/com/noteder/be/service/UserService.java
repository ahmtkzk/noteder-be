package com.noteder.be.service;

import com.noteder.be.dto.UserDto;
import com.noteder.be.entity.User;
import com.noteder.be.entity.UserSettings;
import com.noteder.be.repository.UserRepository;
import com.noteder.be.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDto createUser(User user) {
        // Check if email or username already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.existsByUsername(user.getRealUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Encode password
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        User savedUser = userRepository.save(user);

        // Create default settings
        UserSettings settings = UserSettings.builder()
                .user(savedUser)
                .build();
        userSettingsRepository.save(settings);

        return mapToDto(savedUser);
    }

    public UserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToDto(user);
    }

    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToDto(user);
    }

    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::mapToDto);
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getRealUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
