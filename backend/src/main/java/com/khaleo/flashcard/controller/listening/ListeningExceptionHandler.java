package com.khaleo.flashcard.controller.listening;

import com.khaleo.flashcard.controller.listening.dto.ListeningDtos;
import com.khaleo.flashcard.service.persistence.PersistenceValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice(basePackages = "com.khaleo.flashcard.controller")
public class ListeningExceptionHandler {

    @ExceptionHandler(PersistenceValidationException.class)
    public ResponseEntity<ListeningErrorResponse> handlePersistenceValidation(PersistenceValidationException exception) {
        return ResponseEntity.badRequest()
                .body(new ListeningErrorResponse(exception.getErrorCode().name(), exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ListeningErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(new ListeningErrorResponse("BAD_REQUEST", exception.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ListeningErrorResponse> handleResponseStatus(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        return ResponseEntity.status(status)
                .body(new ListeningErrorResponse(status.name(), exception.getReason()));
    }

    public record ListeningErrorResponse(String code, String message) {
    }
}

