package com.khaleo.flashcard.controller.listening.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ListeningDtos {

    private ListeningDtos() {
    }

    public record TopicDto(
            UUID id,
            @NotBlank String name,
            @NotBlank String slug,
            String description,
            String status,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record ExerciseDto(
            UUID id,
            UUID topicId,
            @NotBlank String name,
            @NotBlank String slug,
            Integer orderIndex,
            String status,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record LessonDto(
            UUID id,
            UUID exerciseId,
            @NotBlank String name,
            @NotBlank String slug,
            Integer orderIndex,
            String mediaUrl,
            String mediaType,
            String status,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record SentenceDto(
            UUID id,
            UUID lessonId,
            Integer orderIndex,
            @NotBlank String transcript,
            String translation,
            String aliasesJson,
            String mediaUrl,
            BigDecimal startTime,
            BigDecimal endTime,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record ReorderSentencesRequest(@NotNull @Size(min = 1) List<UUID> sentenceIds) {
    }

    public record ImportSentencesRequest(@NotNull @Size(min = 1) List<SentenceDto> rows) {
    }

    public record ImportErrorDto(int row, String message) {
    }

    public record ImportSentencesResponse(int successCount, int failedCount, List<ImportErrorDto> errors) {
    }

    public record MediaAccessRequest(@NotBlank String mediaUrl) {
    }

    public record MediaAccessResponse(String url, Instant expiresAt) {
    }

    public record DictionaryLookupEntry(String ipa, String ukAudioUrl, String usAudioUrl, List<String> definitions) {
    }

    public record DictionaryLookupResponse(String term, List<DictionaryLookupEntry> entries) {
    }

    public record DictionaryLookupFailure(String code, String message) {
    }
}

