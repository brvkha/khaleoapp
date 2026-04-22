package com.khaleo.flashcard.contract.listening;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "listening-learner", roles = "USER")
@DisplayName("Learner Listening API Contract Tests")
class LearnerListeningContractTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private TopicRepository topicRepository;
  @Autowired private ExerciseRepository exerciseRepository;
  @Autowired private LessonRepository lessonRepository;
  @Autowired private SentenceRepository sentenceRepository;
  @Autowired private UserRepository userRepository;

  private User testUser;
  private Lesson lesson;
  private Sentence sentence;

  @BeforeEach
  void setUp() {
    ensureFixedUser();

    Topic topic = new Topic();
    topic.setName("Listening Topic");
    topic.setSlug("listening-topic");
    topic.setStatus("published");
    topic = topicRepository.save(topic);

    Exercise exercise = new Exercise();
    exercise.setTopic(topic);
    exercise.setName("Exercise A");
    exercise.setSlug("exercise-a");
    exercise.setOrderIndex(1);
    exercise.setStatus("published");
    exercise = exerciseRepository.save(exercise);

    lesson = new Lesson();
    lesson.setExercise(exercise);
    lesson.setName("Lesson A");
    lesson.setSlug("lesson-a");
    lesson.setOrderIndex(1);
    lesson.setStatus("published");
    lesson = lessonRepository.save(lesson);

    sentence = new Sentence();
    sentence.setLesson(lesson);
    sentence.setOrderIndex(1);
    sentence.setTranscript("The train leaves at seven");
    sentence.setTranslation("Tàu rời đi lúc bảy giờ");
    sentence = sentenceRepository.save(sentence);
  }

  @Test
  @DisplayName("GET /api/v1/listening/lessons/{lessonId}/workspace returns workspace payload")
  void getWorkspaceReturnsPayload() throws Exception {
    mockMvc
        .perform(get("/api/v1/listening/lessons/{lessonId}/workspace", lesson.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.lessonId", equalTo(lesson.getId().toString())))
        .andExpect(jsonPath("$.sentences", hasSize(1)))
        .andExpect(jsonPath("$.sentences[0].id", equalTo(sentence.getId().toString())))
        .andExpect(jsonPath("$.sentences[0].isCompleted", equalTo(false)));
  }

  @Test
  @DisplayName("POST /api/v1/listening/progress/sentences/{sentenceId} accepts correct_check")
  void progressUpdateAcceptsCorrectCheck() throws Exception {
    String payload = objectMapper.writeValueAsString(Map.of("actionType", "correct_check"));

    mockMvc
        .perform(
            post("/api/v1/listening/progress/sentences/{sentenceId}", sentence.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sentenceId", equalTo(sentence.getId().toString())))
        .andExpect(jsonPath("$.completionSource", equalTo("correct_check")))
        .andExpect(jsonPath("$.completed", equalTo(true)));
  }

  @Test
  @DisplayName("POST /api/v1/listening/progress/sentences/{sentenceId} rejects invalid action")
  void progressUpdateRejectsInvalidAction() throws Exception {
    String payload = objectMapper.writeValueAsString(Map.of("actionType", "wrong_action"));

    mockMvc
        .perform(
            post("/api/v1/listening/progress/sentences/{sentenceId}", sentence.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(status().isBadRequest());
  }

  private void ensureFixedUser() {
    testUser = userRepository.findByUsername("listening-learner").orElse(null);
    if (testUser != null) {
      return;
    }

    User user = new User();
    user.setUsername("listening-learner");
    user.setEmail("listening-learner@khaleo.app");
    user.setPasswordHash("$2a$10$abcdefghijklmnopqrstuv");
    user.setRole(UserRole.ROLE_USER);
    user.setIsEmailVerified(true);
    testUser = userRepository.save(user);
  }
}

