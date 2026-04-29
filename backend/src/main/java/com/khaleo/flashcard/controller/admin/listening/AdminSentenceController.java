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
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/admin/listening/sentences")
@RequiredArgsConstructor
public class AdminSentenceController {
    private final AdminListeningCrudService crudService;
    private final AdminSentenceImportService importService;
    private final AdminSentenceReorderService reorderService;
    @GetMapping
    public List<ListeningDtos.SentenceDto> listSentences(@RequestParam(required = false) UUID lessonId) {
        return crudService.listSentences(lessonId);
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ListeningDtos.SentenceDto createSentence(@Valid @RequestBody ListeningDtos.SentenceDto request) {
        return crudService.saveSentence(request.lessonId(), null, request);
    }
    @GetMapping("/{sentenceId}")
    public ListeningDtos.SentenceDto getSentence(@PathVariable UUID sentenceId) {
        return crudService.getSentence(sentenceId);
    }
    @PutMapping("/{sentenceId}")
    public ListeningDtos.SentenceDto updateSentence(@PathVariable UUID sentenceId, @Valid @RequestBody ListeningDtos.SentenceDto request) {
        return crudService.saveSentence(request.lessonId(), sentenceId, request);
    }
    @DeleteMapping("/{sentenceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSentence(@PathVariable UUID sentenceId) {
        crudService.deleteSentence(sentenceId);
    }
    @PostMapping("/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorderSentencesGlobally(@Valid @RequestBody ListeningDtos.ReorderSentencesRequest request) {
        reorderService.reorderViaSentenceContext(request);
    }
    @PostMapping("/import")
    public ListeningDtos.ImportSentencesResponse importSentences(@Valid @RequestBody ListeningDtos.ImportSentencesRequest request) {
        return importService.importSentences(request);
    }
}
