package com.khaleo.flashcard.service.listening;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminSentenceReorderService {

    private final AdminListeningCrudService adminListeningCrudService;

    public void reorder(UUID lessonId, List<UUID> sentenceIds) {
        adminListeningCrudService.reorderSentences(lessonId, sentenceIds);
    }
}

