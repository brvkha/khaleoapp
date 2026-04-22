package com.khaleo.flashcard.controller.listening.dto;

import java.util.UUID;

/**
 * T041 - Learner progress query response.
 */
public record ProgressQueryResponse(
    UUID sentenceId,
    boolean completed,
    String completionSource
) {}

