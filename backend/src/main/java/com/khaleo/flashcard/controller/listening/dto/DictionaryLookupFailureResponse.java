package com.khaleo.flashcard.controller.listening.dto;

/**
 * T054 - Dictionary lookup failure payload.
 */
public record DictionaryLookupFailureResponse(
    String code,
    String message
) {}

