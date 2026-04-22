package com.khaleo.flashcard.service.listening;

import com.khaleo.flashcard.controller.listening.dto.ExerciseListResponse;
import com.khaleo.flashcard.controller.listening.dto.LessonListResponse;
import com.khaleo.flashcard.controller.listening.dto.TopicListResponse;
import com.khaleo.flashcard.entity.Exercise;
import com.khaleo.flashcard.entity.Lesson;
import com.khaleo.flashcard.entity.Topic;
import com.khaleo.flashcard.repository.ExerciseRepository;
import com.khaleo.flashcard.repository.LessonRepository;
import com.khaleo.flashcard.repository.TopicRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for learner catalogue queries (topics, exercises, lessons).
 * T038b - Learner Workspace Implementation
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearnerCatalogueService {

  private static final String PUBLISHED = "published";

  private final TopicRepository topicRepository;
  private final ExerciseRepository exerciseRepository;
  private final LessonRepository lessonRepository;

  /**
   * Get all published topics for learner catalogue.
   */
  public List<TopicListResponse> getPublishedTopics() {
    return topicRepository.findAllByStatus(PUBLISHED).stream()
        .map(this::toTopicListResponse)
        .toList();
  }

  /**
   * Get exercises for a specific topic (slug-based).
   */
  public List<ExerciseListResponse> getExercisesByTopicSlug(String topicSlug) {
    Topic topic = topicRepository.findBySlug(topicSlug)
        .orElseThrow(() -> new IllegalArgumentException("Topic not found: " + topicSlug));

    return exerciseRepository.findAllByTopicIdAndStatus(topic.getId(), PUBLISHED).stream()
        .map(this::toExerciseListResponse)
        .toList();
  }

  /**
   * Get lessons for an exercise (by slug within topic).
   */
  public List<LessonListResponse> getLessonsByExerciseSlug(String topicSlug, String exerciseSlug) {
    Topic topic = topicRepository.findBySlug(topicSlug)
        .orElseThrow(() -> new IllegalArgumentException("Topic not found: " + topicSlug));

    Exercise exercise = exerciseRepository.findByTopicIdAndSlug(topic.getId(), exerciseSlug)
        .orElseThrow(() -> new IllegalArgumentException("Exercise not found: " + exerciseSlug));

    return lessonRepository.findAllByExerciseIdAndStatus(exercise.getId(), PUBLISHED).stream()
        .map(this::toLessonListResponse)
        .toList();
  }

  /**
   * Get lesson by slug (unique within exercise).
   */
  public LessonListResponse getLessonBySlug(String topicSlug, String exerciseSlug, String lessonSlug) {
    Topic topic = topicRepository.findBySlug(topicSlug)
        .orElseThrow(() -> new IllegalArgumentException("Topic not found: " + topicSlug));

    Exercise exercise = exerciseRepository.findByTopicIdAndSlug(topic.getId(), exerciseSlug)
        .orElseThrow(() -> new IllegalArgumentException("Exercise not found: " + exerciseSlug));

    Lesson lesson = lessonRepository.findByExerciseIdAndSlug(exercise.getId(), lessonSlug)
        .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + lessonSlug));

    return toLessonListResponse(lesson);
  }

  /**
   * Get lesson by UUID.
   */
  public LessonListResponse getLessonById(UUID lessonId) {
    Lesson lesson = lessonRepository.findById(lessonId)
        .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + lessonId));
    return toLessonListResponse(lesson);
  }

  private TopicListResponse toTopicListResponse(Topic entity) {
    return new TopicListResponse(
        entity.getId(),
        entity.getName(),
        entity.getSlug(),
        entity.getDescription(),
        entity.getStatus()
    );
  }

  private ExerciseListResponse toExerciseListResponse(Exercise entity) {
    return new ExerciseListResponse(
        entity.getId(),
        entity.getTopic().getId(),
        entity.getName(),
        entity.getSlug(),
        entity.getStatus()
    );
  }

  private LessonListResponse toLessonListResponse(Lesson entity) {
    return new LessonListResponse(
        entity.getId(),
        entity.getExercise().getId(),
        entity.getName(),
        entity.getSlug(),
        entity.getStatus()
    );
  }
}
