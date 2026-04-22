package com.khaleo.flashcard.service.listening;

import com.khaleo.flashcard.controller.listening.dto.DictionaryLookupResponse;
import com.khaleo.flashcard.repository.DictionaryCacheRepository;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DictionaryCacheService {

    private final DictionaryCacheRepository dictionaryCacheRepository;
    private final String provider;
    private final long cacheTtlSeconds;

    public DictionaryCacheService(
            DictionaryCacheRepository dictionaryCacheRepository,
            @Value("${app.listening.dictionary.provider:cambridge}") String provider,
            @Value("${app.listening.dictionary.cache-ttl-seconds:86400}") long cacheTtlSeconds) {
        this.dictionaryCacheRepository = dictionaryCacheRepository;
        this.provider = provider;
        this.cacheTtlSeconds = cacheTtlSeconds;
    }

    public DictionaryLookupResponse getOrLoad(String term, Supplier<DictionaryLookupResponse> loader) {
        String normalizedTerm = normalizeTerm(term);
        Instant now = Instant.now();

        Optional<DictionaryLookupResponse> cached = dictionaryCacheRepository.findValid(provider, normalizedTerm, now);
        if (cached.isPresent()) {
            return cached.get();
        }

        DictionaryLookupResponse loaded = loader.get();
        dictionaryCacheRepository.save(provider, normalizedTerm, loaded, now.plusSeconds(cacheTtlSeconds));
        return loaded;
    }

    private String normalizeTerm(String term) {
        return term == null ? "" : term.trim().toLowerCase(Locale.ROOT);
    }
}

