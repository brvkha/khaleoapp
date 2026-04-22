package com.khaleo.flashcard.controller.listening.dto;

import java.util.UUID;

/**
 * T038a - Learner catalogue topic response.
 */
public record TopicListResponse(
    UUID id,
    String name,
    String slug,
    String description,
    String status
) {}

