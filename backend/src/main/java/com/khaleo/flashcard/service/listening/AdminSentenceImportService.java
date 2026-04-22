package com.khaleo.flashcard.service.listening;

import com.khaleo.flashcard.controller.listening.dto.ListeningDtos;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdminSentenceImportService {

    private final AdminListeningCrudService adminListeningCrudService;

    public ListeningDtos.ImportSentencesResponse importSentences(UUID lessonId, ListeningDtos.ImportSentencesRequest request) {
        List<ListeningDtos.ImportErrorDto> errors = new ArrayList<>();
        int successCount = 0;
        int rowNumber = 1;

        for (ListeningDtos.SentenceDto row : request.rows()) {
            try {
                adminListeningCrudService.saveSentence(lessonId, row.id(), row);
                successCount++;
            } catch (RuntimeException ex) {
                String message = ex.getMessage() == null ? "Sentence import failed." : ex.getMessage();
                errors.add(new ListeningDtos.ImportErrorDto(rowNumber, message));
            }
            rowNumber++;
        }

        int failedCount = request.rows().size() - successCount;
        return new ListeningDtos.ImportSentencesResponse(successCount, failedCount, errors);
    }
}

