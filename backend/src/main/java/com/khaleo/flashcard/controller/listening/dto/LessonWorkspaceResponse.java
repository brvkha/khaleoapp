package com.khaleo.flashcard.controller.listening.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * T038 - Learner lesson workspace response.
 */
public record LessonWorkspaceResponse(
    UUID lessonId,
    String name,
    String slug,
    String mediaUrl,
    String mediaType,
    List<SentenceWithProgressDto> sentences,
    int progressPercent,
    long completedCount,
    long totalCount
) {

  public record SentenceWithProgressDto(
      UUID id,
      Integer orderIndex,
      String transcript,
      String translation,
      String aliasesJson,
      String mediaUrl,
      BigDecimal startTime,
      BigDecimal endTime,
      boolean isCompleted
  ) {}
}

