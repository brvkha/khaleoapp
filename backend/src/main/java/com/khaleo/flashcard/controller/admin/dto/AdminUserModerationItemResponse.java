package com.khaleo.flashcard.controller.admin.dto;

import java.time.Instant;
import java.util.UUID;

public record AdminUserModerationItemResponse(
        UUID id,
        String username,
        String role,
        boolean verified,
        boolean banned,
        Instant createdAt) {
}
