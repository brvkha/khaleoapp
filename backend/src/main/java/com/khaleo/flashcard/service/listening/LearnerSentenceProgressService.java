package com.khaleo.flashcard.service.listening;

import com.khaleo.flashcard.controller.listening.dto.ProgressQueryResponse;
import com.khaleo.flashcard.controller.listening.dto.ProgressUpdateResponse;
import com.khaleo.flashcard.entity.Sentence;
import com.khaleo.flashcard.entity.User;
import com.khaleo.flashcard.entity.UserSentenceProgress;
import com.khaleo.flashcard.repository.SentenceRepository;
import com.khaleo.flashcard.repository.UserRepository;
import com.khaleo.flashcard.repository.UserSentenceProgressRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for updating learner sentence progress.
 * T040 - Upserts progress and calculates completion percentage.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LearnerSentenceProgressService {

  private final UserSentenceProgressRepository progressRepository;
  private final SentenceRepository sentenceRepository;
  private final UserRepository userRepository;

  /**
   * Mark a sentence as completed via correct check or skip action.
   * Only allows CORRECT_CHECK or SKIP as valid completion sources.
   */
  public ProgressUpdateResponse markSentenceComplete(UUID sentenceId, String actionType) {
    if (!isValidAction(actionType)) {
      throw new IllegalArgumentException("Invalid action type. Must be 'correct_check' or 'skip'");
    }

    Sentence sentence = sentenceRepository.findById(sentenceId)
        .orElseThrow(() -> new IllegalArgumentException("Sentence not found: " + sentenceId));

    UUID userId = getCurrentUserId();
    
    // Upsert progress record
     UserSentenceProgress progress = progressRepository.findByUserIdAndSentenceId(userId, sentenceId)
         .orElse(new UserSentenceProgress());

     User user = userRepository.findById(userId)
         .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

     progress.setUser(user);
    progress.setSentence(sentence);
     progress.setIsCompleted(true);
    progress.setCompletionSource(actionType);
    progress.setCompletedAt(Instant.now());
    progress.setUpdatedAt(Instant.now());

    UserSentenceProgress saved = progressRepository.save(progress);

    // Calculate lesson progress
    long lessonCompletedCount = progressRepository
        .countByUserIdAndSentenceLessonIdAndIsCompletedTrue(userId, sentence.getLesson().getId());
    long lessonTotalCount = sentenceRepository.countByLessonId(sentence.getLesson().getId());
    int lessonProgressPercent = lessonTotalCount == 0 ? 0 : 
        (int) ((lessonCompletedCount * 100) / lessonTotalCount);

    return new ProgressUpdateResponse(
        saved.getId(),
        sentenceId,
        true,
        actionType,
        lessonProgressPercent,
        lessonCompletedCount,
        lessonTotalCount
    );
  }

  /**
   * Get current progress for a specific sentence.
   */
  public ProgressQueryResponse getSentenceProgress(UUID sentenceId) {
    UUID userId = getCurrentUserId();
    
    UserSentenceProgress progress = progressRepository
        .findByUserIdAndSentenceId(userId, sentenceId)
        .orElse(null);

     boolean completed = progress != null && Boolean.TRUE.equals(progress.getIsCompleted());
    String completionSource = progress != null ? progress.getCompletionSource() : null;

    return new ProgressQueryResponse(sentenceId, completed, completionSource);
  }


  // ========== HELPERS ==========

  private boolean isValidAction(String action) {
    return "correct_check".equals(action) || "skip".equals(action);
  }

  private UUID getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      throw new IllegalStateException("User not authenticated");
    }
    // In a real app, extract from auth principal
    return UUID.fromString("00000000-0000-0000-0000-000000000000");
  }
}
