package com.khaleo.flashcard.repository;

import com.khaleo.flashcard.entity.UserSentenceProgress;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSentenceProgressRepository extends JpaRepository<UserSentenceProgress, UUID> {
    Optional<UserSentenceProgress> findByUserIdAndSentenceId(UUID userId, UUID sentenceId);

    List<UserSentenceProgress> findByUserIdAndSentenceIdIn(UUID userId, Collection<UUID> sentenceIds);

    long countByUserIdAndSentenceLessonIdAndIsCompletedTrue(UUID userId, UUID lessonId);

    long countByUserIdAndSentenceLessonId(UUID userId, UUID lessonId);
}

