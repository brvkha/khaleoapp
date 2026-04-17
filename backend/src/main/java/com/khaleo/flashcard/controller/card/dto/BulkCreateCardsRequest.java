package com.khaleo.flashcard.controller.card.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record BulkCreateCardsRequest(
        @NotNull @Size(min = 1, max = 500) List<@Valid BulkCreateCardItem> cards) {

    public record BulkCreateCardItem(
            @Min(1) int line,
            @NotBlank String frontContent,
            @NotBlank String backContent) {
    }

    public record BulkRowError(
            @Min(1) int line,
            @NotNull BulkRowErrorCode code,
            @NotBlank String message) {
    }

    public record BulkCreateCardsResponse(
            @Min(0) int successCount,
            @Min(0) int failedCount,
            @NotNull List<BulkRowError> errors) {
    }
}

