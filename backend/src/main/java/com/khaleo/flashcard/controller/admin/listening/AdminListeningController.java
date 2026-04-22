package com.khaleo.flashcard.controller.admin.listening;

import com.khaleo.flashcard.controller.listening.dto.ListeningDtos;
import com.khaleo.flashcard.service.listening.AdminListeningCrudService;
import com.khaleo.flashcard.service.listening.AdminSentenceImportService;
import com.khaleo.flashcard.service.listening.AdminSentenceReorderService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/listening")
@RequiredArgsConstructor
public class AdminListeningController {

    private final AdminListeningCrudService crudService;
    private final AdminSentenceReorderService reorderService;
    private final AdminSentenceImportService importService;

    @GetMapping("/topics")
    public List<ListeningDtos.TopicDto> listTopics() {
        return crudService.listTopics();
    }

    @PostMapping("/topics")
    @ResponseStatus(HttpStatus.CREATED)
    public ListeningDtos.TopicDto createTopic(@Valid @RequestBody ListeningDtos.TopicDto request) {
        return crudService.saveTopic(null, request);
    }

    @GetMapping("/topics/{topicId}")
    public ListeningDtos.TopicDto getTopic(@PathVariable UUID topicId) {
        return crudService.getTopic(topicId);
    }

    @PutMapping("/topics/{topicId}")
    public ListeningDtos.TopicDto updateTopic(@PathVariable UUID topicId, @Valid @RequestBody ListeningDtos.TopicDto request) {
        return crudService.saveTopic(topicId, request);
    }

    @DeleteMapping("/topics/{topicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTopic(@PathVariable UUID topicId) {
        crudService.deleteTopic(topicId);
    }

    @GetMapping("/topics/{topicId}/exercises")
    public List<ListeningDtos.ExerciseDto> listExercises(@PathVariable UUID topicId) {
        return crudService.listExercises(topicId);
    }

    @PostMapping("/topics/{topicId}/exercises")
    @ResponseStatus(HttpStatus.CREATED)
    public ListeningDtos.ExerciseDto createExercise(
            @PathVariable UUID topicId,
            @Valid @RequestBody ListeningDtos.ExerciseDto request) {
        return crudService.saveExercise(topicId, null, request);
    }

    @GetMapping("/exercises/{exerciseId}")
    public ListeningDtos.ExerciseDto getExercise(@PathVariable UUID exerciseId) {
        return crudService.getExercise(exerciseId);
    }

    @PutMapping("/exercises/{exerciseId}")
    public ListeningDtos.ExerciseDto updateExercise(
            @PathVariable UUID exerciseId,
            @Valid @RequestBody ListeningDtos.ExerciseDto request) {
        UUID topicId = request.topicId();
        if (topicId == null) {
            throw new IllegalArgumentException("topicId is required for exercise updates.");
        }
        return crudService.saveExercise(topicId, exerciseId, request);
    }

    @DeleteMapping("/exercises/{exerciseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExercise(@PathVariable UUID exerciseId) {
        crudService.deleteExercise(exerciseId);
    }

    @GetMapping("/exercises/{exerciseId}/lessons")
    public List<ListeningDtos.LessonDto> listLessons(@PathVariable UUID exerciseId) {
        return crudService.listLessons(exerciseId);
    }

    @PostMapping("/exercises/{exerciseId}/lessons")
    @ResponseStatus(HttpStatus.CREATED)
    public ListeningDtos.LessonDto createLesson(
            @PathVariable UUID exerciseId,
            @Valid @RequestBody ListeningDtos.LessonDto request) {
        return crudService.saveLesson(exerciseId, null, request);
    }

    @GetMapping("/lessons/{lessonId}")
    public ListeningDtos.LessonDto getLesson(@PathVariable UUID lessonId) {
        return crudService.getLesson(lessonId);
    }

    @PutMapping("/lessons/{lessonId}")
    public ListeningDtos.LessonDto updateLesson(
            @PathVariable UUID lessonId,
            @Valid @RequestBody ListeningDtos.LessonDto request) {
        UUID exerciseId = request.exerciseId();
        if (exerciseId == null) {
            throw new IllegalArgumentException("exerciseId is required for lesson updates.");
        }
        return crudService.saveLesson(exerciseId, lessonId, request);
    }

    @DeleteMapping("/lessons/{lessonId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLesson(@PathVariable UUID lessonId) {
        crudService.deleteLesson(lessonId);
    }

    @GetMapping("/lessons/{lessonId}/sentences")
    public List<ListeningDtos.SentenceDto> listSentences(@PathVariable UUID lessonId) {
        return crudService.listSentences(lessonId);
    }

    @PostMapping("/lessons/{lessonId}/sentences")
    @ResponseStatus(HttpStatus.CREATED)
    public ListeningDtos.SentenceDto createSentence(
            @PathVariable UUID lessonId,
            @Valid @RequestBody ListeningDtos.SentenceDto request) {
        return crudService.saveSentence(lessonId, null, request);
    }

    @GetMapping("/sentences/{sentenceId}")
    public ListeningDtos.SentenceDto getSentence(@PathVariable UUID sentenceId) {
        return crudService.getSentence(sentenceId);
    }

    @PutMapping("/sentences/{sentenceId}")
    public ListeningDtos.SentenceDto updateSentence(
            @PathVariable UUID sentenceId,
            @Valid @RequestBody ListeningDtos.SentenceDto request) {
        UUID lessonId = request.lessonId();
        if (lessonId == null) {
            throw new IllegalArgumentException("lessonId is required for sentence updates.");
        }
        return crudService.saveSentence(lessonId, sentenceId, request);
    }

    @DeleteMapping("/sentences/{sentenceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSentence(@PathVariable UUID sentenceId) {
        crudService.deleteSentence(sentenceId);
    }

    @PutMapping("/lessons/{lessonId}/sentences/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorderSentences(
            @PathVariable UUID lessonId,
            @Valid @RequestBody ListeningDtos.ReorderSentencesRequest request) {
        reorderService.reorder(lessonId, request.sentenceIds());
    }

    @PostMapping("/lessons/{lessonId}/sentences/import-json")
    public ListeningDtos.ImportSentencesResponse importSentences(
            @PathVariable UUID lessonId,
            @Valid @RequestBody ListeningDtos.ImportSentencesRequest request) {
        return importService.importSentences(lessonId, request);
    }
}

