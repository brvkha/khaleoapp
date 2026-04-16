package com.khaleo.flashcard.controller.folder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record FolderCreateRequest(
        @NotBlank @Size(max = 100) String name,
        @JsonProperty("parent_id") UUID parentId) {
}

