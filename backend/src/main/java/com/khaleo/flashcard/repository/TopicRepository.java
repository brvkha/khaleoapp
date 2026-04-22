package com.khaleo.flashcard.repository;

import com.khaleo.flashcard.entity.Topic;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, UUID> {
    List<Topic> findAllByStatus(String status);

    Optional<Topic> findBySlug(String slug);
}

