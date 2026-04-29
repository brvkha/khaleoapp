package com.khaleo.flashcard.service.listening;
import java.util.List;
import java.util.UUID;
import com.khaleo.flashcard.repository.SentenceRepository;
import com.khaleo.flashcard.entity.Sentence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class AdminSentenceReorderService {
    private final AdminListeningCrudService adminListeningCrudService;
    private final SentenceRepository sentenceRepository;
    public void reorderViaLessonContext(UUID lessonId, com.khaleo.flashcard.controller.listening.dto.ListeningDtos.ReorderSentencesRequest request) {
        adminListeningCrudService.reorderSentences(lessonId, request.sentenceIds());
    }
    public void reorderViaSentenceContext(com.khaleo.flashcard.controller.listening.dto.ListeningDtos.ReorderSentencesRequest request) {
        if (request.sentenceIds() == null || request.sentenceIds().isEmpty()) return;
        Sentence sentence = sentenceRepository.findById(request.sentenceIds().get(0)).orElseThrow();
        adminListeningCrudService.reorderSentences(sentence.getLesson().getId(), request.sentenceIds());
    }
}
