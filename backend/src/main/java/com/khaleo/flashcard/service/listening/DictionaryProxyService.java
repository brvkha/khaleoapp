package com.khaleo.flashcard.service.listening;

import com.khaleo.flashcard.controller.listening.dto.DictionaryLookupResponse;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class DictionaryProxyService {

    private final DictionaryCacheService dictionaryCacheService;

    public DictionaryLookupResponse lookup(String term) {
        String normalizedTerm = normalizeTerm(term);
        return dictionaryCacheService.getOrLoad(normalizedTerm, () -> fetchFromProvider(normalizedTerm));
    }

    private DictionaryLookupResponse fetchFromProvider(String term) {
        return new DictionaryLookupResponse(
                term,
                List.of(new DictionaryLookupResponse.Entry(null, null, null, List.of())));
    }

    private String normalizeTerm(String term) {
        if (term == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "term is required.");
        }

        String normalized = term.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "term is required.");
        }

        if (normalized.length() > 80) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "term is too long.");
        }

        return normalized;
    }
}

