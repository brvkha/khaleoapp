package com.khaleo.flashcard.service.listening;
import com.khaleo.flashcard.controller.listening.dto.ListeningDtos;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class AdminSentenceImportService {
    private final AdminListeningCrudService adminListeningCrudService;
    public ListeningDtos.ImportSentencesResponse importSentences(ListeningDtos.ImportSentencesRequest request) {
        List<ListeningDtos.ImportErrorDto> errors = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;
        int rowNumber = 1;
        for (ListeningDtos.SentenceDto row : request.rows()) {
            try {
                adminListeningCrudService.saveSentence(row.lessonId(), row.id(), row);
                successCount++;
            } catch (Exception ex) {
                failedCount++;
                String message = ex.getMessage() == null ? "Sentence import failed" : ex.getMessage();
                errors.add(new ListeningDtos.ImportErrorDto(rowNumber, message));
            }
            rowNumber++;
        }
        return new ListeningDtos.ImportSentencesResponse(successCount, failedCount, errors);
    }
}
