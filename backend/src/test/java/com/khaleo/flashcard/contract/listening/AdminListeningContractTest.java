package com.khaleo.flashcard.contract.listening;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.khaleo.flashcard.controller.listening.dto.ListeningDtos;
import com.khaleo.flashcard.entity.*;
import com.khaleo.flashcard.repository.*;
import java.math.BigDecimal;
import java.util.List;
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
import org.springframework.transaction.annotation.Transactional;

/**
 * Contract tests for Admin Listening CMS API endpoints.
 * Validates CRUD operations and reorder/import contracts.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = "ADMIN")
@DisplayName("Admin Listening API Contract Tests")
class AdminListeningContractTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private TopicRepository topicRepository;

  @Autowired private ExerciseRepository exerciseRepository;

  @Autowired private LessonRepository lessonRepository;

  @Autowired private SentenceRepository sentenceRepository;

  private Topic testTopic;

  @BeforeEach
  void setUp() {
    testTopic = new Topic();
    testTopic.setName("Test Topic");
    testTopic.setSlug("test-topic");
    testTopic.setDescription("Test Description");
    testTopic.setStatus("draft");
    testTopic = topicRepository.save(testTopic);
  }

  // ========== TOPIC CRUD TESTS ==========

  @Test
  @DisplayName("POST /api/v1/admin/listening/topics creates topic and returns 201")
  void createTopicReturns201() throws Exception {
    ListeningDtos.TopicDto request = new ListeningDtos.TopicDto(
        null, "IELTS Listening", "ielts-listening", "Official IELTS listening materials", "draft", null, null);

    mockMvc
        .perform(
            post("/api/v1/admin/listening/topics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.name", equalTo("IELTS Listening")))
        .andExpect(jsonPath("$.slug", equalTo("ielts-listening")))
        .andExpect(jsonPath("$.status", equalTo("draft")));
  }

  @Test
  @DisplayName("GET /api/v1/admin/listening/topics returns all topics")
  void getTopicsReturnsAll() throws Exception {
    mockMvc
        .perform(get("/api/v1/admin/listening/topics").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
        .andExpect(jsonPath("$[0].id", notNullValue()));
  }

  @Test
  @DisplayName("GET /api/v1/admin/listening/topics/{id} returns topic by ID")
  void getTopicByIdReturnsOk() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/admin/listening/topics/" + testTopic.getId())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", equalTo(testTopic.getId().toString())))
        .andExpect(jsonPath("$.name", equalTo(testTopic.getName())));
  }

  @Test
  @DisplayName("PUT /api/v1/admin/listening/topics/{id} updates topic and returns 200")
  void updateTopicReturns200() throws Exception {
    ListeningDtos.TopicDto request = new ListeningDtos.TopicDto(
        null, "Updated IELTS Listening", "updated-ielts", "Updated description", "published", null, null);

    mockMvc
        .perform(
            put("/api/v1/admin/listening/topics/" + testTopic.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name", equalTo("Updated IELTS Listening")))
        .andExpect(jsonPath("$.status", equalTo("published")));
  }

  @Test
  @DisplayName("DELETE /api/v1/admin/listening/topics/{id} returns 204 and deletes topic")
  void deleteTopicReturns204() throws Exception {
    Topic topic = new Topic();
    topic.setName("Delete Me");
    topic.setSlug("delete-me");
    topic.setStatus("draft");
    topic = topicRepository.save(topic);

    mockMvc
        .perform(delete("/api/v1/admin/listening/topics/" + topic.getId()))
        .andExpect(status().isNoContent());

    // Verify deletion
    mockMvc
        .perform(get("/api/v1/admin/listening/topics/" + topic.getId()))
        .andExpect(status().isNotFound());
  }

  // ========== EXERCISE CRUD TESTS ==========

  @Test
  @DisplayName("POST /api/v1/admin/listening/topics/{topicId}/exercises creates exercise")
  void createExerciseReturns201() throws Exception {
    ListeningDtos.ExerciseDto request = new ListeningDtos.ExerciseDto(
        null, testTopic.getId(), "Cambridge Test 20", "cambridge-20", null, "draft", null, null);

    mockMvc
        .perform(
            post("/api/v1/admin/listening/topics/" + testTopic.getId() + "/exercises")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.name", equalTo("Cambridge Test 20")))
        .andExpect(jsonPath("$.topicId", equalTo(testTopic.getId().toString())));
  }

  @Test
  @DisplayName("GET /api/v1/admin/listening/topics/{topicId}/exercises returns exercises")
  void getExercisesReturnsAll() throws Exception {
    Exercise exercise = new Exercise();
    exercise.setTopic(testTopic);
    exercise.setName("Test Exercise");
    exercise.setSlug("test-ex");
    exercise.setOrderIndex(1);
    exercise.setStatus("draft");
    exerciseRepository.save(exercise);

    mockMvc
        .perform(
            get("/api/v1/admin/listening/topics/" + testTopic.getId() + "/exercises")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
  }

  @Test
  @DisplayName("PUT /api/v1/admin/listening/exercises/{id} updates exercise")
  void updateExerciseReturns200() throws Exception {
    Exercise exercise = new Exercise();
    exercise.setTopic(testTopic);
    exercise.setName("Old Name");
    exercise.setSlug("old-slug");
    exercise.setOrderIndex(1);
    exercise.setStatus("draft");
    exercise = exerciseRepository.save(exercise);

    ListeningDtos.ExerciseDto request = new ListeningDtos.ExerciseDto(
        exercise.getId(), testTopic.getId(), "New Name", "new-slug", null, "published", null, null);

    mockMvc
        .perform(
            put("/api/v1/admin/listening/exercises/" + exercise.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name", equalTo("New Name")));
  }

  @Test
  @DisplayName("DELETE /api/v1/admin/listening/exercises/{id} deletes exercise")
  void deleteExerciseReturns204() throws Exception {
    Exercise exercise = new Exercise();
    exercise.setTopic(testTopic);
    exercise.setName("Delete Me");
    exercise.setSlug("delete-me-ex");
    exercise.setOrderIndex(1);
    exercise.setStatus("draft");
    exercise = exerciseRepository.save(exercise);

    mockMvc
        .perform(delete("/api/v1/admin/listening/exercises/" + exercise.getId()))
        .andExpect(status().isNoContent());
  }

  // ========== LESSON CRUD TESTS ==========

  @Test
  @DisplayName("POST /api/v1/admin/listening/exercises/{exerciseId}/lessons creates lesson")
  void createLessonReturns201() throws Exception {
    Exercise exercise = new Exercise();
    exercise.setTopic(testTopic);
    exercise.setName("Test Exercise");
    exercise.setSlug("test-ex");
    exercise.setOrderIndex(1);
    exercise.setStatus("draft");
    exercise = exerciseRepository.save(exercise);

    ListeningDtos.LessonDto request = new ListeningDtos.LessonDto(
        null, exercise.getId(), "Test 1 - Part 1", "test-1-part-1", null, null, null, "draft", null, null);

    mockMvc
        .perform(
            post("/api/v1/admin/listening/exercises/" + exercise.getId() + "/lessons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.name", equalTo("Test 1 - Part 1")))
        .andExpect(jsonPath("$.exerciseId", equalTo(exercise.getId().toString())));
  }

  @Test
  @DisplayName("GET /api/v1/admin/listening/exercises/{exerciseId}/lessons returns lessons")
  void getLessonsReturnsAll() throws Exception {
    Exercise exercise = new Exercise();
    exercise.setTopic(testTopic);
    exercise.setName("Test Exercise");
    exercise.setSlug("test-ex");
    exercise.setOrderIndex(1);
    exercise.setStatus("draft");
    exercise = exerciseRepository.save(exercise);

    Lesson lesson = new Lesson();
    lesson.setExercise(exercise);
    lesson.setName("Test Lesson");
    lesson.setSlug("test-lesson");
    lesson.setOrderIndex(1);
    lesson.setStatus("draft");
    lessonRepository.save(lesson);

    mockMvc
        .perform(
            get("/api/v1/admin/listening/exercises/" + exercise.getId() + "/lessons")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
  }

  @Test
  @DisplayName("PUT /api/v1/admin/listening/lessons/{id} updates lesson")
  void updateLessonReturns200() throws Exception {
    Exercise exercise = new Exercise();
    exercise.setTopic(testTopic);
    exercise.setName("Test Exercise");
    exercise.setSlug("test-ex");
    exercise.setOrderIndex(1);
    exercise.setStatus("draft");
    exercise = exerciseRepository.save(exercise);

    Lesson lesson = new Lesson();
    lesson.setExercise(exercise);
    lesson.setName("Old Name");
    lesson.setSlug("old-slug");
    lesson.setOrderIndex(1);
    lesson.setStatus("draft");
    lesson = lessonRepository.save(lesson);

    ListeningDtos.LessonDto request = new ListeningDtos.LessonDto(
        lesson.getId(), exercise.getId(), "New Name", "new-slug", null, null, null, "published", null, null);

    mockMvc
        .perform(
            put("/api/v1/admin/listening/lessons/" + lesson.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name", equalTo("New Name")));
  }

  @Test
  @DisplayName("DELETE /api/v1/admin/listening/lessons/{id} deletes lesson")
  void deleteLessonReturns204() throws Exception {
    Exercise exercise = new Exercise();
    exercise.setTopic(testTopic);
    exercise.setName("Test Exercise");
    exercise.setSlug("test-ex");
    exercise.setOrderIndex(1);
    exercise.setStatus("draft");
    exercise = exerciseRepository.save(exercise);

    Lesson lesson = new Lesson();
    lesson.setExercise(exercise);
    lesson.setName("Delete Me");
    lesson.setSlug("delete-me");
    lesson.setOrderIndex(1);
    lesson.setStatus("draft");
    lesson = lessonRepository.save(lesson);

    mockMvc
        .perform(delete("/api/v1/admin/listening/lessons/" + lesson.getId()))
        .andExpect(status().isNoContent());
  }

  // ========== SENTENCE CRUD TESTS ==========

  @Test
  @DisplayName("POST /api/v1/admin/listening/lessons/{lessonId}/sentences creates sentence")
  void createSentenceReturns201() throws Exception {
    Exercise exercise = new Exercise();
    exercise.setTopic(testTopic);
    exercise.setName("Test Exercise");
    exercise.setSlug("test-ex");
    exercise.setOrderIndex(1);
    exercise.setStatus("draft");
    exercise = exerciseRepository.save(exercise);

    Lesson lesson = new Lesson();
    lesson.setExercise(exercise);
    lesson.setName("Test Lesson");
    lesson.setSlug("test-lesson");
    lesson.setOrderIndex(1);
    lesson.setStatus("draft");
    lesson = lessonRepository.save(lesson);

    ListeningDtos.SentenceDto request = new ListeningDtos.SentenceDto(
        null, lesson.getId(), null, "The cat sat on the mat", "Con mèo ngồi trên thảm", "[\"the cat sat\"]", null, null, null, null, null);

    mockMvc
        .perform(
            post("/api/v1/admin/listening/lessons/" + lesson.getId() + "/sentences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.transcript", equalTo("The cat sat on the mat")))
        .andExpect(jsonPath("$.translation", equalTo("Con mèo ngồi trên thảm")));
  }

  @Test
  @DisplayName("GET /api/v1/admin/listening/lessons/{lessonId}/sentences returns sentences")
  void getSentencesReturnsAll() throws Exception {
    Exercise exercise = new Exercise();
    exercise.setTopic(testTopic);
    exercise.setName("Test Exercise");
    exercise.setSlug("test-ex");
    exercise.setOrderIndex(1);
    exercise.setStatus("draft");
    exercise = exerciseRepository.save(exercise);

    Lesson lesson = new Lesson();
    lesson.setExercise(exercise);
    lesson.setName("Test Lesson");
    lesson.setSlug("test-lesson");
    lesson.setOrderIndex(1);
    lesson.setStatus("draft");
    lesson = lessonRepository.save(lesson);

    Sentence sentence = new Sentence();
    sentence.setLesson(lesson);
    sentence.setOrderIndex(1);
    sentence.setTranscript("Test sentence");
    sentenceRepository.save(sentence);

    mockMvc
        .perform(
            get("/api/v1/admin/listening/lessons/" + lesson.getId() + "/sentences")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
  }

  @Test
  @DisplayName("PUT /api/v1/admin/listening/sentences/{id} updates sentence")
  void updateSentenceReturns200() throws Exception {
    Exercise exercise = new Exercise();
    exercise.setTopic(testTopic);
    exercise.setName("Test Exercise");
    exercise.setSlug("test-ex");
    exercise.setOrderIndex(1);
    exercise.setStatus("draft");
    exercise = exerciseRepository.save(exercise);

    Lesson lesson = new Lesson();
    lesson.setExercise(exercise);
    lesson.setName("Test Lesson");
    lesson.setSlug("test-lesson");
    lesson.setOrderIndex(1);
    lesson.setStatus("draft");
    lesson = lessonRepository.save(lesson);

    Sentence sentence = new Sentence();
    sentence.setLesson(lesson);
    sentence.setOrderIndex(1);
    sentence.setTranscript("Old text");
    sentence = sentenceRepository.save(sentence);

    ListeningDtos.SentenceDto request = new ListeningDtos.SentenceDto(
        sentence.getId(), lesson.getId(), null, "New text", "New translation", null, null, null, null, null, null);

    mockMvc
        .perform(
            put("/api/v1/admin/listening/sentences/" + sentence.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.transcript", equalTo("New text")));
  }

  @Test
  @DisplayName("DELETE /api/v1/admin/listening/sentences/{id} deletes sentence")
  void deleteSentenceReturns204() throws Exception {
    Exercise exercise = new Exercise();
    exercise.setTopic(testTopic);
    exercise.setName("Test Exercise");
    exercise.setSlug("test-ex");
    exercise.setOrderIndex(1);
    exercise.setStatus("draft");
    exercise = exerciseRepository.save(exercise);

    Lesson lesson = new Lesson();
    lesson.setExercise(exercise);
    lesson.setName("Test Lesson");
    lesson.setSlug("test-lesson");
    lesson.setOrderIndex(1);
    lesson.setStatus("draft");
    lesson = lessonRepository.save(lesson);

    Sentence sentence = new Sentence();
    sentence.setLesson(lesson);
    sentence.setOrderIndex(1);
    sentence.setTranscript("Delete me");
    sentence = sentenceRepository.save(sentence);

    mockMvc
        .perform(delete("/api/v1/admin/listening/sentences/" + sentence.getId()))
        .andExpect(status().isNoContent());
  }

  // ========== VALIDATION TESTS ==========

  @Test
  @DisplayName("POST topic without required slug returns 400")
  void createTopicWithoutSlugReturns400() throws Exception {
    ListeningDtos.TopicDto request = new ListeningDtos.TopicDto(
        null, "No Slug", null, null, "draft", null, null);

    mockMvc
        .perform(
            post("/api/v1/admin/listening/topics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST sentence without transcript returns 400")
  void createSentenceWithoutTranscriptReturns400() throws Exception {
    Exercise exercise = new Exercise();
    exercise.setTopic(testTopic);
    exercise.setName("Test Exercise");
    exercise.setSlug("test-ex");
    exercise.setOrderIndex(1);
    exercise.setStatus("draft");
    exercise = exerciseRepository.save(exercise);

    Lesson lesson = new Lesson();
    lesson.setExercise(exercise);
    lesson.setName("Test Lesson");
    lesson.setSlug("test-lesson");
    lesson.setOrderIndex(1);
    lesson.setStatus("draft");
    lesson = lessonRepository.save(lesson);

    ListeningDtos.SentenceDto request = new ListeningDtos.SentenceDto(
        null, lesson.getId(), null, null, "Has translation but no transcript", null, null, null, null, null, null);

    mockMvc
        .perform(
            post("/api/v1/admin/listening/lessons/" + lesson.getId() + "/sentences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}











