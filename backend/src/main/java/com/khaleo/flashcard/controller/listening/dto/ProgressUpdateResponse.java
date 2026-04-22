package com.khaleo.flashcard.controller.listening.dto;

import java.util.UUID;

/**
 * T041 - Learner progress update response.
 */
public record ProgressUpdateResponse(
    UUID progressId,
    UUID sentenceId,
    boolean completed,
    String completionSource,
    int lessonProgressPercent,
    long lessonCompletedCount,
    long lessonTotalCount
) {}

