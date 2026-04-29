package com.khaleo.flashcard.controller.admin.listening.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.util.UUID;
public record AdminSentenceRequest(
    @NotNull UUID lessonId,
    @NotBlank String originalText,
    String translation,
    Integer orderIndex,
    @Min(0) Integer startTime,
    @Min(0) Integer endTime
) {}
