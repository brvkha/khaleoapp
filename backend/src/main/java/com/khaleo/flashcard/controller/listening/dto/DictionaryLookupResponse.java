package com.khaleo.flashcard.controller.listening.dto;

import java.util.List;

/**
 * T054 - Dictionary lookup success payload.
 */
public record DictionaryLookupResponse(
    String term,
    List<Entry> entries
) {

  public record Entry(
      String ipa,
      String ukAudioUrl,
      String usAudioUrl,
      List<String> definitions
  ) {}
}

