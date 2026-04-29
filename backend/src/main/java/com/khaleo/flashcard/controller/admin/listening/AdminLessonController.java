package com.khaleo.flashcard.controller.admin.listening;
import com.khaleo.flashcard.controller.listening.dto.ListeningDtos;
import com.khaleo.flashcard.service.listening.AdminListeningCrudService;
import com.khaleo.flashcard.service.listening.AdminSentenceReorderService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/admin/listening/lessons")
@RequiredArgsConstructor
public class AdminLessonController {
    private final AdminListeningCrudService crudService;
    private final AdminSentenceReorderService reorderService;
    @GetMapping
    public List<ListeningDtos.LessonDto> listLessons(@RequestParam(required = false) UUID exerciseId) {
        return crudService.listLessons(exerciseId);
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ListeningDtos.LessonDto createLesson(@Valid @RequestBody ListeningDtos.LessonDto request) {
        return crudService.saveLesson(request.exerciseId(), null, request);
    }
    @GetMapping("/{lessonId}")
    public ListeningDtos.LessonDto getLesson(@PathVariable UUID lessonId) {
        return crudService.getLesson(lessonId);
    }
    @PutMapping("/{lessonId}")
    public ListeningDtos.LessonDto updateLesson(@PathVariable UUID lessonId, @Valid @RequestBody ListeningDtos.LessonDto request) {
        return crudService.saveLesson(request.exerciseId(), lessonId, request);
    }
    @DeleteMapping("/{lessonId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLesson(@PathVariable UUID lessonId) {
        crudService.deleteLesson(lessonId);
    }
    @PostMapping("/{lessonId}/sentences/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorderSentencesWithinLesson(@PathVariable UUID lessonId, @Valid @RequestBody ListeningDtos.ReorderSentencesRequest request) {
        reorderService.reorderViaLessonContext(lessonId, request);
    }
}
