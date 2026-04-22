package com.khaleo.flashcard.repository;

import com.khaleo.flashcard.entity.Sentence;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SentenceRepository extends JpaRepository<Sentence, UUID> {
    List<Sentence> findAllByLessonIdOrderByOrderIndexAsc(UUID lessonId);

    List<Sentence> findByLessonIdOrderByOrderIndexAsc(UUID lessonId);

    long countByLessonId(UUID lessonId);
}

