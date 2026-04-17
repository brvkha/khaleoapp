package com.khaleo.flashcard.service.auth;

import com.khaleo.flashcard.entity.User;
import com.khaleo.flashcard.entity.enums.UserRole;
import com.khaleo.flashcard.repository.UserRepository;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@SuppressWarnings("null")
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthAuditLogger authAuditLogger;

    public RegisterResult registerGuest(String username, String email, String rawPassword) {
        String normalizedUsername = normalizeUsername(username);
        String normalizedEmail = normalizeEmail(email);

        if (normalizedUsername.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Username and password are required.");
        }

        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new AuthDomainException(HttpStatus.CONFLICT, AuthErrorCode.INVALID_REQUEST, "Username already exists.");
        }

        if (normalizedEmail != null && userRepository.existsByEmail(normalizedEmail)) {
            throw new AuthDomainException(HttpStatus.CONFLICT, AuthErrorCode.DUPLICATE_EMAIL, "Email already exists.");
        }

        User user = User.builder()
                .username(normalizedUsername)
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(UserRole.ROLE_USER)
                .isEmailVerified(Boolean.FALSE)
                .failedLoginAttempts(0)
                .build();

        User savedUser = userRepository.save(user);
        authAuditLogger.logEvent(
                "auth_registration_completed",
                Map.of("userId", savedUser.getId(), "username", savedUser.getUsername()));

        return new RegisterResult(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        return normalized.isBlank() ? null : normalized;
    }

    public record RegisterResult(UUID userId, String username, String email) {
    }
}
