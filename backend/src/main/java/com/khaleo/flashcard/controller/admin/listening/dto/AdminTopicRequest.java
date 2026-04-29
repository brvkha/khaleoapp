package com.khaleo.flashcard.controller.admin.listening.dto;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
public record AdminTopicRequest(
    @NotBlank String name,
    @NotBlank String slug,
    String description,
    String status
) {}
