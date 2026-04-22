package com.khaleo.flashcard.service.listening;

import com.khaleo.flashcard.controller.listening.dto.LessonWorkspaceResponse;
import com.khaleo.flashcard.entity.*;
import com.khaleo.flashcard.repository.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for learner workspace queries.
 * T039 - Returns lesson with sentences and learner progress summary.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearnerListeningWorkspaceService {

  private final LessonRepository lessonRepository;
  private final SentenceRepository sentenceRepository;
  private final UserSentenceProgressRepository progressRepository;
  private final UserRepository userRepository;

  /**
   * Get lesson workspace with all sentences and learner's progress state.
   */
  public LessonWorkspaceResponse getLessonWorkspace(UUID lessonId) {
    Lesson lesson = lessonRepository.findById(lessonId)
        .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + lessonId));

    UUID userId = getCurrentUserId();
    List<Sentence> sentences = sentenceRepository.findByLessonIdOrderByOrderIndexAsc(lessonId);

    // Fetch learner's progress for all sentences in this lesson
    Map<UUID, UserSentenceProgress> progressMap = progressRepository
        .findByUserIdAndSentenceIdIn(userId, sentences.stream().map(Sentence::getId).toList())
        .stream()
        .collect(Collectors.toMap(p -> p.getSentence().getId(), p -> p));

    // Build sentence DTOs with progress info
    List<LessonWorkspaceResponse.SentenceWithProgressDto> sentenceDtos = sentences.stream()
        .map(s -> new LessonWorkspaceResponse.SentenceWithProgressDto(
            s.getId(),
            s.getOrderIndex(),
            s.getTranscript(),
            s.getTranslation(),
            s.getAliasesJson(),
            s.getMediaUrl(),
            s.getStartTime(),
            s.getEndTime(),
            progressMap.containsKey(s.getId()) && Boolean.TRUE.equals(progressMap.get(s.getId()).getIsCompleted())
        ))
        .toList();

    // Calculate progress percentage
    long completedCount = sentenceDtos.stream().filter(LessonWorkspaceResponse.SentenceWithProgressDto::isCompleted).count();
    int progressPercent = sentences.isEmpty() ? 0 : (int) ((completedCount * 100) / sentences.size());

    return new LessonWorkspaceResponse(
        lesson.getId(),
        lesson.getName(),
        lesson.getSlug(),
        lesson.getMediaUrl(),
        lesson.getMediaType(),
        sentenceDtos,
        progressPercent,
        completedCount,
        sentences.size()
    );
  }


  // ========== HELPERS ==========

  private UUID getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      throw new IllegalStateException("User not authenticated");
    }

    Object principal = auth.getPrincipal();
    if (principal instanceof UUID uuid) {
      return uuid;
    }
    if (principal instanceof String principalString) {
      return parseAuthenticatedUserId(principalString);
    }
    if (principal instanceof UserDetails userDetails) {
      return parseAuthenticatedUserId(userDetails.getUsername());
    }

    return parseAuthenticatedUserId(auth.getName());
  }

  private UUID parseAuthenticatedUserId(String rawUserId) {
    if (rawUserId == null || rawUserId.isBlank() || "anonymousUser".equals(rawUserId)) {
      throw new IllegalStateException("User not authenticated");
    }

    try {
      return UUID.fromString(rawUserId.trim());
    } catch (IllegalArgumentException ex) {
      return userRepository.findByUsername(rawUserId.trim())
          .map(User::getId)
          .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + rawUserId, ex));
    }
  }
}
