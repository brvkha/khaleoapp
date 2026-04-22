package com.khaleo.flashcard.repository;

import com.khaleo.flashcard.entity.Lesson;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    List<Lesson> findByExerciseIdOrderByOrderIndexAsc(UUID exerciseId);

    List<Lesson> findAllByExerciseIdAndStatus(UUID exerciseId, String status);

    Optional<Lesson> findByExerciseIdAndSlug(UUID exerciseId, String slug);

    Optional<Lesson> findBySlug(String slug);
}

