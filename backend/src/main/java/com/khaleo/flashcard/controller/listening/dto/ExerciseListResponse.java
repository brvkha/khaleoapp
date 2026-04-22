package com.khaleo.flashcard.controller.listening.dto;

import java.util.UUID;

/**
 * T038a - Learner catalogue exercise response.
 */
public record ExerciseListResponse(
    UUID id,
    UUID topicId,
    String name,
    String slug,
    String status
) {}

