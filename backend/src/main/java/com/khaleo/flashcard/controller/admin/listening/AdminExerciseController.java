package com.khaleo.flashcard.controller.admin.listening;
import com.khaleo.flashcard.controller.listening.dto.ListeningDtos;
import com.khaleo.flashcard.service.listening.AdminListeningCrudService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/admin/listening/exercises")
@RequiredArgsConstructor
public class AdminExerciseController {
    private final AdminListeningCrudService crudService;
    @GetMapping
    public List<ListeningDtos.ExerciseDto> listExercises(@RequestParam(required = false) UUID topicId) {
        return crudService.listExercises(topicId);
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ListeningDtos.ExerciseDto createExercise(@Valid @RequestBody ListeningDtos.ExerciseDto request) {
        return crudService.saveExercise(request.topicId(), null, request);
    }
    @GetMapping("/{exerciseId}")
    public ListeningDtos.ExerciseDto getExercise(@PathVariable UUID exerciseId) {
        return crudService.getExercise(exerciseId);
    }
    @PutMapping("/{exerciseId}")
    public ListeningDtos.ExerciseDto updateExercise(@PathVariable UUID exerciseId, @Valid @RequestBody ListeningDtos.ExerciseDto request) {
        return crudService.saveExercise(request.topicId(), exerciseId, request);
    }
    @DeleteMapping("/{exerciseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExercise(@PathVariable UUID exerciseId) {
        crudService.deleteExercise(exerciseId);
    }
}
