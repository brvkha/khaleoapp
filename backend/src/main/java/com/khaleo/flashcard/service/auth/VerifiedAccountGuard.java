package com.khaleo.flashcard.service.auth;

import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class VerifiedAccountGuard {

    public void requireVerified(UUID userId, String operation, String resourceType, String resourceKey) {
        // Email verification is intentionally disabled for now.
    }
}
