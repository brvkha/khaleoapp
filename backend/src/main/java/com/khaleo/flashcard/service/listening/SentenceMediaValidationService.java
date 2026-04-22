package com.khaleo.flashcard.service.listening;

import com.khaleo.flashcard.service.persistence.PersistenceValidationException;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class SentenceMediaValidationService {

    public void validateSharedMediaWindow(BigDecimal startTime, BigDecimal endTime) {
        if (startTime == null && endTime == null) {
            return;
        }
        if (startTime == null || endTime == null) {
            throw invalid("Sentence shared-media timestamps require both startTime and endTime.");
        }
        if (startTime.signum() < 0) {
            throw invalid("Sentence startTime must be greater than or equal to zero.");
        }
        if (endTime.compareTo(startTime) <= 0) {
            throw invalid("Sentence endTime must be greater than startTime.");
        }
    }

    private PersistenceValidationException invalid(String message) {
        return new PersistenceValidationException(
                PersistenceValidationException.PersistenceErrorCode.VALIDATION_REJECTED,
                message);
    }
}

