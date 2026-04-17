package com.khaleo.flashcard.controller.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank String username,
        String email,
        @NotBlank String password) {
}
