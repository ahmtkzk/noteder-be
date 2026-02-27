package com.noteder.be.service;

import com.noteder.be.dto.AuthenticationRequest;
import com.noteder.be.dto.AuthenticationResponse;
import com.noteder.be.dto.RegisterRequest;
import com.noteder.be.dto.UserDto;
import com.noteder.be.entity.User;
import com.noteder.be.entity.UserSettings;
import com.noteder.be.repository.UserRepository;
import com.noteder.be.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        var savedUser = userRepository.save(user);

        // Create default settings
        UserSettings settings = UserSettings.builder()
                .user(savedUser)
                .build();
        userSettingsRepository.save(settings);

        var jwtToken = jwtService.generateToken(savedUser);
        var refreshToken = jwtService.generateRefreshToken(savedUser);

        // TODO: Save refresh token to DB

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .tokenType("Bearer")
                .user(mapToDto(savedUser))
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        String email = request.getEmailOrUsername();

        // If input doesn't look like an email, try to resolve it as a username
        if (!email.contains("@")) {
            var user = userRepository.findByUsername(request.getEmailOrUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            email = user.getEmail();
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.getPassword()
                )
        );

        var user = userRepository.findByEmail(email)
                .orElseThrow();

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        // TODO: Save/Update refresh token in DB

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .tokenType("Bearer")
                .user(mapToDto(user))
                .build();
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
