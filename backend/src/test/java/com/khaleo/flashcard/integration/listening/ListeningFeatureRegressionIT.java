package com.khaleo.flashcard.integration.listening;

import static org.assertj.core.api.Assertions.assertThat;

import com.khaleo.flashcard.controller.listening.dto.LessonWorkspaceResponse;
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
import com.khaleo.flashcard.service.listening.LearnerListeningWorkspaceService;
import com.khaleo.flashcard.service.listening.LearnerSentenceProgressService;
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
@DisplayName("Listening Feature Regression")
class ListeningFeatureRegressionIT {

  @Autowired private TopicRepository topicRepository;
  @Autowired private ExerciseRepository exerciseRepository;
  @Autowired private LessonRepository lessonRepository;
  @Autowired private SentenceRepository sentenceRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private LearnerListeningWorkspaceService learnerListeningWorkspaceService;
  @Autowired private LearnerSentenceProgressService learnerSentenceProgressService;

  private User testUser;
  private Lesson lesson;
  private Sentence sentence;

  @BeforeEach
  void setUp() {
    ensureFixedUser();
    SecurityContextHolder.getContext().setAuthentication(
        new TestingAuthenticationToken(testUser.getUsername(), "n/a", "ROLE_USER"));

    Topic topic = new Topic();
    topic.setName("Regression Topic");
    topic.setSlug("regression-topic");
    topic.setStatus("published");
    topic = topicRepository.save(topic);

    Exercise exercise = new Exercise();
    exercise.setTopic(topic);
    exercise.setName("Regression Exercise");
    exercise.setSlug("regression-exercise");
    exercise.setOrderIndex(1);
    exercise.setStatus("published");
    exercise = exerciseRepository.save(exercise);

    lesson = new Lesson();
    lesson.setExercise(exercise);
    lesson.setName("Regression Lesson");
    lesson.setSlug("regression-lesson");
    lesson.setOrderIndex(1);
    lesson.setStatus("published");
    lesson = lessonRepository.save(lesson);

    sentence = new Sentence();
    sentence.setLesson(lesson);
    sentence.setOrderIndex(1);
    sentence.setTranscript("Regression sentence");
    sentence = sentenceRepository.save(sentence);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("workspace and progress update stay consistent")
  void workspaceAndProgressStayConsistent() {
    LessonWorkspaceResponse before = learnerListeningWorkspaceService.getLessonWorkspace(lesson.getId());
    assertThat(before.totalCount()).isEqualTo(1);
    assertThat(before.progressPercent()).isEqualTo(0);

    ProgressUpdateResponse updated = learnerSentenceProgressService.markSentenceComplete(sentence.getId(), "skip");
    assertThat(updated.lessonProgressPercent()).isEqualTo(100);

    LessonWorkspaceResponse after = learnerListeningWorkspaceService.getLessonWorkspace(lesson.getId());
    assertThat(after.progressPercent()).isEqualTo(100);
    assertThat(after.completedCount()).isEqualTo(1);
    assertThat(after.sentences().get(0).isCompleted()).isTrue();
  }

  private void ensureFixedUser() {
    testUser = userRepository.findByUsername("regression-learner").orElse(null);
    if (testUser != null) {
      return;
    }

    User user = new User();
    user.setUsername("regression-learner");
    user.setEmail("regression-learner@khaleo.app");
    user.setPasswordHash("$2a$10$abcdefghijklmnopqrstuv");
    user.setRole(UserRole.ROLE_USER);
    user.setIsEmailVerified(true);
    testUser = userRepository.save(user);
  }
}

