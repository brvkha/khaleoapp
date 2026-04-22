package com.khaleo.flashcard.controller.listening;

import com.khaleo.flashcard.controller.listening.dto.LessonWorkspaceResponse;
import com.khaleo.flashcard.controller.listening.dto.ProgressQueryResponse;
import com.khaleo.flashcard.controller.listening.dto.ProgressUpdateRequest;
import com.khaleo.flashcard.controller.listening.dto.ProgressUpdateResponse;
import com.khaleo.flashcard.service.listening.LearnerListeningWorkspaceService;
import com.khaleo.flashcard.service.listening.LearnerSentenceProgressService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/listening")
@RequiredArgsConstructor
public class LearnerListeningController {

  private final LearnerListeningWorkspaceService learnerListeningWorkspaceService;
  private final LearnerSentenceProgressService learnerSentenceProgressService;

  @GetMapping("/lessons/{lessonId}/workspace")
  public LessonWorkspaceResponse getLessonWorkspace(@PathVariable UUID lessonId) {
    return learnerListeningWorkspaceService.getLessonWorkspace(lessonId);
  }

  @PostMapping({"/progress/sentences/{sentenceId}", "/sentences/{sentenceId}/complete"})
  public ProgressUpdateResponse markSentenceComplete(
      @PathVariable UUID sentenceId,
      @Valid @RequestBody ProgressUpdateRequest request
  ) {
    return learnerSentenceProgressService.markSentenceComplete(sentenceId, request.actionType());
  }

  @GetMapping("/progress/sentences/{sentenceId}")
  public ProgressQueryResponse getSentenceProgress(@PathVariable UUID sentenceId) {
    return learnerSentenceProgressService.getSentenceProgress(sentenceId);
  }
}

