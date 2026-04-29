package com.khaleo.flashcard.controller.admin.listening.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
public record AdminLessonRequest(
    @NotNull UUID exerciseId,
    @NotBlank String title,
    @NotBlank String slug,
    Integer orderIndex,
    String audioUrl
) {}
