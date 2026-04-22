package com.khaleo.flashcard.repository;

import com.khaleo.flashcard.entity.Exercise;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {
    List<Exercise> findByTopicIdOrderByOrderIndexAsc(UUID topicId);

    List<Exercise> findAllByTopicIdAndStatus(UUID topicId, String status);

    Optional<Exercise> findByTopicIdAndSlug(UUID topicId, String slug);
}

