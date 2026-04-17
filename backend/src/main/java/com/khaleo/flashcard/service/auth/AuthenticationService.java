package com.khaleo.flashcard.service.auth;

import com.khaleo.flashcard.entity.RefreshToken;
import com.khaleo.flashcard.entity.User;
import com.khaleo.flashcard.repository.RefreshTokenRepository;
import com.khaleo.flashcard.repository.UserRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@SuppressWarnings("null")
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthAuditLogger authAuditLogger;
    private final LoginLockoutService loginLockoutService;
    private final JwtTokenService jwtTokenService;

    @Value("${app.auth.jwt.refresh-token-ttl-days:7}")
    private long refreshTokenTtlDays;

    public LoginResult login(String identifier, String rawPassword) {
        User user = authenticateUser(identifier, rawPassword);

        String refreshTokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiresAt(Instant.now().plusSeconds(refreshTokenTtlDays * 24 * 60 * 60))
                .build();
        refreshTokenRepository.save(refreshToken);

        Map<String, Object> claims = buildAccessClaims(user);
        String accessToken = jwtTokenService.createAccessToken(user.getId().toString(), claims);

        authAuditLogger.logEvent("auth_login_success", Map.of("userId", user.getId(), "username", user.getUsername()));
        return new LoginResult(accessToken, refreshTokenValue, jwtTokenService.accessTokenTtlSeconds());
    }

    public User authenticateUser(String identifier, String rawPassword) {
        String normalizedIdentifier = normalizeIdentifier(identifier);
        User user = userRepository.findByUsernameOrEmail(normalizedIdentifier, normalizedIdentifier)
                .orElseThrow(() -> invalidCredentials(normalizedIdentifier));

        if (user.getBannedAt() != null) {
            authAuditLogger.logEvent("auth_login_blocked_banned", Map.of("identifier", normalizedIdentifier));
            throw new AuthDomainException(HttpStatus.FORBIDDEN, AuthErrorCode.BANNED_USER_REQUEST_DENIED, "Banned account access denied.");
        }

        if (loginLockoutService.isCurrentlyLocked(user)) {
            authAuditLogger.logEvent("auth_login_blocked_locked", Map.of("identifier", normalizedIdentifier));
            loginLockoutService.ensureNotLocked(user);
        }

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            loginLockoutService.onFailedAttempt(user);
            if (loginLockoutService.isCurrentlyLocked(user)) {
                authAuditLogger.logEvent("auth_account_locked", Map.of("identifier", normalizedIdentifier));
            }
            userRepository.save(user);
            throw invalidCredentials(normalizedIdentifier);
        }

        if (user.getFailedLoginAttempts() > 0 || user.getAccountLockedUntil() != null) {
            loginLockoutService.onSuccessfulLogin(user);
            userRepository.save(user);
        }

        authAuditLogger.logEvent("auth_login_verified", Map.of("userId", user.getId(), "username", user.getUsername()));
        return user;
    }

    private AuthDomainException invalidCredentials(String normalizedIdentifier) {
        authAuditLogger.logEvent("auth_login_failed", Map.of("identifier", normalizedIdentifier));
        return new AuthDomainException(HttpStatus.UNAUTHORIZED, AuthErrorCode.INVALID_CREDENTIALS, "Invalid credentials.");
    }

    private Map<String, Object> buildAccessClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("username", user.getUsername());
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            claims.put("email", user.getEmail());
        }
        return claims;
    }

    private String normalizeIdentifier(String identifier) {
        return identifier == null ? "" : identifier.trim().toLowerCase(Locale.ROOT);
    }

    public record LoginResult(String accessToken, String refreshToken, long expiresIn) {
    }
}
