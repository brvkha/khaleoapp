package com.khaleo.flashcard.service.listening;

import com.khaleo.flashcard.controller.listening.dto.ListeningDtos;
import com.khaleo.flashcard.entity.Exercise;
import com.khaleo.flashcard.entity.Lesson;
import com.khaleo.flashcard.entity.Sentence;
import com.khaleo.flashcard.entity.Topic;
import com.khaleo.flashcard.entity.User;
import com.khaleo.flashcard.repository.ExerciseRepository;
import com.khaleo.flashcard.repository.LessonRepository;
import com.khaleo.flashcard.repository.SentenceRepository;
import com.khaleo.flashcard.repository.TopicRepository;
import com.khaleo.flashcard.repository.UserSentenceProgressRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminListeningCrudService {

    private final TopicRepository topicRepository;
    private final ExerciseRepository exerciseRepository;
    private final LessonRepository lessonRepository;
    private final SentenceRepository sentenceRepository;
    private final UserSentenceProgressRepository progressRepository;
    private final SentenceMediaValidationService sentenceMediaValidationService;

    @Transactional(readOnly = true)
    public List<ListeningDtos.TopicDto> listTopics() {
        return topicRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream().map(this::toTopicDto).toList();
    }

    @Transactional(readOnly = true)
    public ListeningDtos.TopicDto getTopic(UUID topicId) {
        return toTopicDto(findTopic(topicId));
    }

    public ListeningDtos.TopicDto saveTopic(UUID topicId, ListeningDtos.TopicDto request) {
        Topic topic = topicId == null ? new Topic() : findTopic(topicId);
        String slug = normalizeSlug(request.slug());
        ensureTopicSlugAvailable(slug, topic.getId());

        topic.setName(trimToNull(request.name()));
        topic.setSlug(slug);
        topic.setDescription(trimToNull(request.description()));
        topic.setStatus(defaultIfBlank(request.status(), "draft"));
        return toTopicDto(topicRepository.save(topic));
    }

    public void deleteTopic(UUID topicId) {
        topicRepository.delete(findTopic(topicId));
    }

    @Transactional(readOnly = true)
    public List<ListeningDtos.ExerciseDto> listExercises(UUID topicId) { if (topicId == null) { return exerciseRepository.findAll().stream().map(this::toExerciseDto).toList(); }
        findTopic(topicId);
        return exerciseRepository.findByTopicIdOrderByOrderIndexAsc(topicId).stream().map(this::toExerciseDto).toList();
    }

    @Transactional(readOnly = true)
    public ListeningDtos.ExerciseDto getExercise(UUID exerciseId) {
        return toExerciseDto(findExercise(exerciseId));
    }

    public ListeningDtos.ExerciseDto saveExercise(UUID topicId, UUID exerciseId, ListeningDtos.ExerciseDto request) {
        Topic topic = findTopic(topicId);
        Exercise exercise = exerciseId == null ? new Exercise() : findExercise(exerciseId);
        String slug = normalizeSlug(request.slug());
        ensureExerciseSlugAvailable(topic.getId(), slug, exercise.getId());

        exercise.setTopic(topic);
        exercise.setName(trimToNull(request.name()));
        exercise.setSlug(slug);
        exercise.setOrderIndex(resolveOrderIndex(request.orderIndex(), exerciseRepository.findByTopicIdOrderByOrderIndexAsc(topicId)));
        exercise.setStatus(defaultIfBlank(request.status(), "draft"));
        return toExerciseDto(exerciseRepository.save(exercise));
    }

    public void deleteExercise(UUID exerciseId) {
        exerciseRepository.delete(findExercise(exerciseId));
    }

    @Transactional(readOnly = true)
    public List<ListeningDtos.LessonDto> listLessons(UUID exerciseId) { if (exerciseId == null) { return lessonRepository.findAll().stream().map(this::toLessonDto).toList(); }
        findExercise(exerciseId);
        return lessonRepository.findByExerciseIdOrderByOrderIndexAsc(exerciseId).stream().map(this::toLessonDto).toList();
    }

    @Transactional(readOnly = true)
    public ListeningDtos.LessonDto getLesson(UUID lessonId) {
        return toLessonDto(findLesson(lessonId));
    }

    public ListeningDtos.LessonDto saveLesson(UUID exerciseId, UUID lessonId, ListeningDtos.LessonDto request) {
        Exercise exercise = findExercise(exerciseId);
        Lesson lesson = lessonId == null ? new Lesson() : findLesson(lessonId);
        String slug = normalizeSlug(request.slug());
        ensureLessonSlugAvailable(exercise.getId(), slug, lesson.getId());

        lesson.setExercise(exercise);
        lesson.setName(trimToNull(request.name()));
        lesson.setSlug(slug);
        lesson.setOrderIndex(resolveOrderIndex(request.orderIndex(), lessonRepository.findByExerciseIdOrderByOrderIndexAsc(exerciseId)));
        lesson.setMediaUrl(trimToNull(request.mediaUrl()));
        lesson.setMediaType(trimToNull(request.mediaType()));
        lesson.setStatus(defaultIfBlank(request.status(), "draft"));
        return toLessonDto(lessonRepository.save(lesson));
    }

    public void deleteLesson(UUID lessonId) {
        lessonRepository.delete(findLesson(lessonId));
    }

    @Transactional(readOnly = true)
    public List<ListeningDtos.SentenceDto> listSentences(UUID lessonId) { if (lessonId == null) { return sentenceRepository.findAll().stream().map(this::toSentenceDto).toList(); }
        findLesson(lessonId);
        return sentenceRepository.findByLessonIdOrderByOrderIndexAsc(lessonId).stream().map(this::toSentenceDto).toList();
    }

    @Transactional(readOnly = true)
    public ListeningDtos.SentenceDto getSentence(UUID sentenceId) {
        return toSentenceDto(findSentence(sentenceId));
    }

    public ListeningDtos.SentenceDto saveSentence(UUID lessonId, UUID sentenceId, ListeningDtos.SentenceDto request) {
        Lesson lesson = findLesson(lessonId);
        Sentence sentence = sentenceId == null ? new Sentence() : findSentence(sentenceId);
        sentenceMediaValidationService.validateSharedMediaWindow(request.startTime(), request.endTime());

        sentence.setLesson(lesson);
        sentence.setOrderIndex(resolveOrderIndex(request.orderIndex(), sentenceRepository.findByLessonIdOrderByOrderIndexAsc(lessonId)));
        sentence.setTranscript(trimToNull(request.transcript()));
        sentence.setTranslation(trimToNull(request.translation()));
        sentence.setAliasesJson(trimToNull(request.aliasesJson()));
        sentence.setMediaUrl(trimToNull(request.mediaUrl()));
        sentence.setStartTime(request.startTime());
        sentence.setEndTime(request.endTime());
        return toSentenceDto(sentenceRepository.save(sentence));
    }

    public void deleteSentence(UUID sentenceId) {
        sentenceRepository.delete(findSentence(sentenceId));
    }

    public void reorderSentences(UUID lessonId, List<UUID> sentenceIds) {
        List<Sentence> sentences = sentenceRepository.findByLessonIdOrderByOrderIndexAsc(lessonId);
        if (sentences.size() != sentenceIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sentence reorder payload must include every sentence in the lesson.");
        }

        List<UUID> currentIds = sentences.stream().map(Sentence::getId).toList();
        if (!currentIds.containsAll(sentenceIds) || !sentenceIds.containsAll(currentIds)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sentence reorder payload contains unknown sentence IDs.");
        }

        List<Sentence> ordered = new ArrayList<>(sentences.size());
        for (int i = 0; i < sentenceIds.size(); i++) {
            UUID id = sentenceIds.get(i);
            Sentence sentence = sentences.stream().filter(item -> item.getId().equals(id)).findFirst().orElseThrow();
            sentence.setOrderIndex(i + 1);
            ordered.add(sentence);
        }
        sentenceRepository.saveAll(ordered);
    }

    @Transactional(readOnly = true)
    public long completedSentenceCount(UUID userId, UUID lessonId) {
        return progressRepository.countByUserIdAndSentenceLessonIdAndIsCompletedTrue(userId, lessonId);
    }

    @Transactional(readOnly = true)
    public long totalSentenceCount(UUID userId, UUID lessonId) {
        return progressRepository.countByUserIdAndSentenceLessonId(userId, lessonId);
    }

    private Topic findTopic(UUID topicId) {
        return topicRepository.findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found: " + topicId));
    }

    private Exercise findExercise(UUID exerciseId) {
        return exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found: " + exerciseId));
    }

    private Lesson findLesson(UUID lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found: " + lessonId));
    }

    private Sentence findSentence(UUID sentenceId) {
        return sentenceRepository.findById(sentenceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sentence not found: " + sentenceId));
    }

    private void ensureTopicSlugAvailable(String slug, UUID currentId) {
        topicRepository.findBySlug(slug).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Topic slug already exists: " + slug);
            }
        });
    }

    private void ensureExerciseSlugAvailable(UUID topicId, String slug, UUID currentId) {
        exerciseRepository.findByTopicIdAndSlug(topicId, slug).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Exercise slug already exists in this topic: " + slug);
            }
        });
    }

    private void ensureLessonSlugAvailable(UUID exerciseId, String slug, UUID currentId) {
        lessonRepository.findByExerciseIdAndSlug(exerciseId, slug).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Lesson slug already exists in this exercise: " + slug);
            }
        });
    }

    private Integer resolveOrderIndex(Integer requestedOrderIndex, List<?> existing) {
        if (requestedOrderIndex != null && requestedOrderIndex > 0) {
            return requestedOrderIndex;
        }
        return existing.size() + 1;
    }

    private String normalizeSlug(String slug) {
        String value = trimToNull(slug);
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slug is required.");
        }
        String normalized = value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
        if (normalized.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slug is required.");
        }
        return normalized;
    }

    private String defaultIfBlank(String value, String fallback) {
        String trimmed = trimToNull(value);
        return trimmed == null ? fallback : trimmed;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ListeningDtos.TopicDto toTopicDto(Topic topic) {
        return new ListeningDtos.TopicDto(
                topic.getId(),
                topic.getName(),
                topic.getSlug(),
                topic.getDescription(),
                topic.getStatus(),
                topic.getCreatedAt(),
                topic.getUpdatedAt());
    }

    private ListeningDtos.ExerciseDto toExerciseDto(Exercise exercise) {
        return new ListeningDtos.ExerciseDto(
                exercise.getId(),
                exercise.getTopic() == null ? null : exercise.getTopic().getId(),
                exercise.getName(),
                exercise.getSlug(),
                exercise.getOrderIndex(),
                exercise.getStatus(),
                exercise.getCreatedAt(),
                exercise.getUpdatedAt());
    }

    private ListeningDtos.LessonDto toLessonDto(Lesson lesson) {
        return new ListeningDtos.LessonDto(
                lesson.getId(),
                lesson.getExercise() == null ? null : lesson.getExercise().getId(),
                lesson.getName(),
                lesson.getSlug(),
                lesson.getOrderIndex(),
                lesson.getMediaUrl(),
                lesson.getMediaType(),
                lesson.getStatus(),
                lesson.getCreatedAt(),
                lesson.getUpdatedAt());
    }

    private ListeningDtos.SentenceDto toSentenceDto(Sentence sentence) {
        return new ListeningDtos.SentenceDto(
                sentence.getId(),
                sentence.getLesson() == null ? null : sentence.getLesson().getId(),
                sentence.getOrderIndex(),
                sentence.getTranscript(),
                sentence.getTranslation(),
                sentence.getAliasesJson(),
                sentence.getMediaUrl(),
                sentence.getStartTime(),
                sentence.getEndTime(),
                sentence.getCreatedAt(),
                sentence.getUpdatedAt());
    }
}


