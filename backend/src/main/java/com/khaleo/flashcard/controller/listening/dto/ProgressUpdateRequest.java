package com.khaleo.flashcard.controller.listening.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * T038 - Learner progress update request.
 */
public record ProgressUpdateRequest(
    @NotBlank
    @Pattern(regexp = "correct_check|skip")
    String actionType
) {}

