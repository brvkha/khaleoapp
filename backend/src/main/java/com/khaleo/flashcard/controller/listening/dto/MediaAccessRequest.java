package com.khaleo.flashcard.controller.listening.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * T053 - Media access request payload.
 */
public record MediaAccessRequest(
    @NotBlank String mediaUrl
) {}

