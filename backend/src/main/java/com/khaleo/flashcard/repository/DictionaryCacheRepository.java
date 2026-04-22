package com.khaleo.flashcard.repository;

import com.khaleo.flashcard.controller.listening.dto.DictionaryLookupResponse;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class DictionaryCacheRepository {

    private final Map<String, CachedDictionaryEntry> cache = new ConcurrentHashMap<>();

    public Optional<DictionaryLookupResponse> findValid(String provider, String normalizedTerm, Instant now) {
        CachedDictionaryEntry entry = cache.get(key(provider, normalizedTerm));
        if (entry == null || entry.expiresAt().isBefore(now)) {
            return Optional.empty();
        }
        return Optional.of(entry.payload());
    }

    public void save(String provider, String normalizedTerm, DictionaryLookupResponse payload, Instant expiresAt) {
        cache.put(key(provider, normalizedTerm), new CachedDictionaryEntry(payload, expiresAt));
    }

    private String key(String provider, String normalizedTerm) {
        return provider + "::" + normalizedTerm;
    }

    private record CachedDictionaryEntry(DictionaryLookupResponse payload, Instant expiresAt) {}
}

