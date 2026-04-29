package com.khaleo.flashcard.controller.admin.listening.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
public record AdminExerciseRequest(
    @NotNull UUID topicId,
    @NotBlank String name,
    @NotBlank String slug,
    String description,
    String status,
    Integer orderIndex
) {}
