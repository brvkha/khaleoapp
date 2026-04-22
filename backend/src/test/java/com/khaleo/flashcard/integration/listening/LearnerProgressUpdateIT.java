package com.khaleo.flashcard.integration.listening;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.khaleo.flashcard.controller.listening.dto.ProgressUpdateResponse;
import com.khaleo.flashcard.entity.Exercise;
import com.khaleo.flashcard.entity.Lesson;
import com.khaleo.flashcard.entity.Sentence;
import com.khaleo.flashcard.entity.Topic;
import com.khaleo.flashcard.entity.User;
import com.khaleo.flashcard.entity.enums.UserRole;
import com.khaleo.flashcard.repository.ExerciseRepository;
import com.khaleo.flashcard.repository.LessonRepository;
import com.khaleo.flashcard.repository.SentenceRepository;
import com.khaleo.flashcard.repository.TopicRepository;
import com.khaleo.flashcard.repository.UserRepository;
import com.khaleo.flashcard.repository.UserSentenceProgressRepository;
import com.khaleo.flashcard.service.listening.LearnerSentenceProgressService;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Learner Progress Update Integration Tests")
class LearnerProgressUpdateIT {

  private static final UUID FIXED_TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

  @Autowired private LearnerSentenceProgressService learnerSentenceProgressService;
  @Autowired private UserSentenceProgressRepository progressRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private TopicRepository topicRepository;
  @Autowired private ExerciseRepository exerciseRepository;
  @Autowired private LessonRepository lessonRepository;
  @Autowired private SentenceRepository sentenceRepository;

  private Sentence sentence;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("test", "n/a", "ROLE_USER"));
    ensureFixedUser();

    Topic topic = new Topic();
    topic.setName("Topic");
    topic.setSlug("topic-progress");
    topic.setStatus("published");
    topic = topicRepository.save(topic);

    Exercise exercise = new Exercise();
    exercise.setTopic(topic);
    exercise.setName("Exercise");
    exercise.setSlug("exercise-progress");
    exercise.setOrderIndex(0);
    exercise.setStatus("published");
    exercise = exerciseRepository.save(exercise);

    Lesson lesson = new Lesson();
    lesson.setExercise(exercise);
    lesson.setName("Lesson");
    lesson.setSlug("lesson-progress");
    lesson.setOrderIndex(0);
    lesson.setStatus("published");
    lesson = lessonRepository.save(lesson);

    sentence = new Sentence();
    sentence.setLesson(lesson);
    sentence.setOrderIndex(0);
    sentence.setTranscript("This is one sentence");
    sentence = sentenceRepository.save(sentence);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("marks sentence complete for correct_check")
  void marksSentenceCompleteForCorrectCheck() {
    ProgressUpdateResponse response = learnerSentenceProgressService.markSentenceComplete(sentence.getId(), "correct_check");

    assertThat(response.sentenceId()).isEqualTo(sentence.getId());
    assertThat(response.completionSource()).isEqualTo("correct_check");
    assertThat(response.completed()).isTrue();
    assertThat(progressRepository.findByUserIdAndSentenceId(FIXED_TEST_USER_ID, sentence.getId())).isPresent();
  }

  @Test
  @DisplayName("rejects invalid action and keeps progress unchanged")
  void rejectsInvalidActionAndKeepsProgressUnchanged() {
    assertThatThrownBy(() -> learnerSentenceProgressService.markSentenceComplete(sentence.getId(), "incorrect_check"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid action type");

    assertThat(progressRepository.findByUserIdAndSentenceId(FIXED_TEST_USER_ID, sentence.getId())).isEmpty();
  }

  private void ensureFixedUser() {
    if (userRepository.findById(FIXED_TEST_USER_ID).isPresent()) {
      return;
    }

    User user = new User();
    user.setId(FIXED_TEST_USER_ID);
    user.setUsername("progress-learner");
    user.setEmail("progress-learner@khaleo.app");
    user.setPasswordHash("$2a$10$abcdefghijklmnopqrstuv");
    user.setRole(UserRole.ROLE_USER);
    user.setIsEmailVerified(true);
    userRepository.save(user);
  }
}

