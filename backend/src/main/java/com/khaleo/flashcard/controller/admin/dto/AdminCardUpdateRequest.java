package com.khaleo.flashcard.controller.admin.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Size;

public record AdminCardUpdateRequest(
        @Size(max = 5000) @JsonAlias({"frontText", "term"}) String frontContent,
        @Size(max = 2048) @JsonAlias({"frontMediaUrl", "imageUrl"}) String frontMediaUrl,
        @Size(max = 5000) @JsonAlias({"backText", "answer"}) String backContent,
        @Size(max = 2048) @JsonAlias({"backMediaUrl", "imageUrl"}) String backMediaUrl) {
}
