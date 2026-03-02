package com.noteder.be.service;

import com.noteder.be.dto.AuthenticationRequest;
import com.noteder.be.dto.AuthenticationResponse;
import com.noteder.be.dto.RefreshTokenDto;
import com.noteder.be.dto.RegisterRequest;
import com.noteder.be.dto.UserDto;
import com.noteder.be.entity.User;
import com.noteder.be.entity.UserSettings;
import com.noteder.be.repository.UserRepository;
import com.noteder.be.repository.UserSettingsRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final UserSessionService userSessionService;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${jwt.remember-me-expiration}")
    private long rememberMeExpiration;

    public AuthenticationResponse register(RegisterRequest request, HttpServletRequest servletRequest) {
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

        UserSettings settings = UserSettings.builder()
                .user(savedUser)
                .build();
        userSettingsRepository.save(settings);

        var jwtToken = jwtService.generateToken(savedUser);
        var refreshToken = jwtService.generateRefreshToken(savedUser, false);

        LocalDateTime expiresAt = LocalDateTime.now().plus(refreshExpiration, ChronoUnit.MILLIS);
        refreshTokenService.createRefreshToken(savedUser.getId(), refreshToken, expiresAt);

        String userAgent = servletRequest.getHeader("User-Agent");
        String ipAddress = servletRequest.getRemoteAddr();
        userSessionService.createSession(savedUser.getId(), refreshToken, userAgent, ipAddress, expiresAt);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .tokenType("Bearer")
                .user(mapToDto(savedUser))
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletRequest servletRequest) {
        String email = request.getEmailOrUsername();

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
        var refreshToken = jwtService.generateRefreshToken(user, request.isRememberMe());

        long expirationTime = request.isRememberMe() ? rememberMeExpiration : refreshExpiration;
        LocalDateTime expiresAt = LocalDateTime.now().plus(expirationTime, ChronoUnit.MILLIS);
        refreshTokenService.createRefreshToken(user.getId(), refreshToken, expiresAt);

        String userAgent = servletRequest.getHeader("User-Agent");
        String ipAddress = servletRequest.getRemoteAddr();
        userSessionService.createSession(user.getId(), refreshToken, userAgent, ipAddress, expiresAt);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .tokenType("Bearer")
                .user(mapToDto(user))
                .build();
    }

    public AuthenticationResponse refreshToken(String refreshToken) {
        RefreshTokenDto storedToken = refreshTokenService.getByTokenHash(refreshToken);
        
        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!jwtService.isTokenValid(refreshToken, user)) {
             throw new RuntimeException("Invalid refresh token");
        }

        var newAccessToken = jwtService.generateToken(user);
        
        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .user(mapToDto(user))
                .build();
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
