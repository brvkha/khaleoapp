package com.khaleo.flashcard.controller.listening.dto;

import java.time.Instant;

/**
 * T053 - Media access response payload.
 */
public record MediaAccessResponse(
    String url,
    Instant expiresAt
) {}

