package com.khaleo.flashcard.controller.listening;

import com.khaleo.flashcard.controller.listening.dto.ExerciseListResponse;
import com.khaleo.flashcard.controller.listening.dto.LessonListResponse;
import com.khaleo.flashcard.controller.listening.dto.TopicListResponse;
import com.khaleo.flashcard.service.listening.LearnerCatalogueService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/listening")
@RequiredArgsConstructor
public class LearnerCatalogueController {

  private final LearnerCatalogueService learnerCatalogueService;

  @GetMapping("/topics")
  public List<TopicListResponse> getTopics() {
    return learnerCatalogueService.getPublishedTopics();
  }

  @GetMapping("/topics/{topicSlug}/exercises")
  public List<ExerciseListResponse> getExercises(@PathVariable String topicSlug) {
    return learnerCatalogueService.getExercisesByTopicSlug(topicSlug);
  }

  @GetMapping("/topics/{topicSlug}/exercises/{exerciseSlug}/lessons")
  public List<LessonListResponse> getLessons(
      @PathVariable String topicSlug,
      @PathVariable String exerciseSlug
  ) {
    return learnerCatalogueService.getLessonsByExerciseSlug(topicSlug, exerciseSlug);
  }
}

