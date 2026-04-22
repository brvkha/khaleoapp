package com.khaleo.flashcard.service.listening;

import com.khaleo.flashcard.controller.listening.dto.DictionaryLookupResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DictionaryProxyService {

    private final DictionaryCacheService dictionaryCacheService;

    public DictionaryLookupResponse lookup(String term) {
        return dictionaryCacheService.getOrLoad(term, () -> fetchFromProvider(term));
    }

    private DictionaryLookupResponse fetchFromProvider(String term) {
        return new DictionaryLookupResponse(
                term,
                List.of(new DictionaryLookupResponse.Entry(null, null, null, List.of())));
    }
}

