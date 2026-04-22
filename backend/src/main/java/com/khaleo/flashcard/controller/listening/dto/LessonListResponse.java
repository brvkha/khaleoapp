package com.khaleo.flashcard.controller.listening.dto;

import java.util.UUID;

/**
 * T038a - Learner catalogue lesson response.
 */
public record LessonListResponse(
    UUID id,
    UUID exerciseId,
    String name,
    String slug,
    String status
) {}

