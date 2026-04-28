package com.khaleo.flashcard.controller.listening;

import com.khaleo.flashcard.controller.listening.dto.DictionaryLookupFailureResponse;
import com.khaleo.flashcard.controller.listening.dto.DictionaryLookupResponse;
import com.khaleo.flashcard.controller.listening.dto.MediaAccessRequest;
import com.khaleo.flashcard.controller.listening.dto.MediaAccessResponse;
import com.khaleo.flashcard.service.listening.DictionaryProxyService;
import com.khaleo.flashcard.service.listening.ListeningMediaAccessService;
import com.khaleo.flashcard.service.listening.ListeningStructuredLogger;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/listening")
@RequiredArgsConstructor
public class ListeningSupportController {

    private final ListeningMediaAccessService listeningMediaAccessService;
    private final DictionaryProxyService dictionaryProxyService;
    private final ListeningStructuredLogger listeningStructuredLogger;

    @PostMapping("/media/access")
    public MediaAccessResponse issueMediaAccess(@Valid @RequestBody MediaAccessRequest request) {
        listeningStructuredLogger.info("listening_media_access_request_received", Map.of("mediaUrl", request.mediaUrl()));
        return listeningMediaAccessService.issueAccessUrl(request.mediaUrl());
    }

    @GetMapping("/dictionary")
    public ResponseEntity<?> dictionaryLookup(@RequestParam String term) {
        try {
            DictionaryLookupResponse response = dictionaryProxyService.lookup(term);
            listeningStructuredLogger.info("listening_dictionary_lookup_succeeded", Map.of("term", term));
            return ResponseEntity.ok(response);
        } catch (IllegalStateException exception) {
            listeningStructuredLogger.warn("listening_dictionary_lookup_failed", Map.of("term", term, "reason", exception.getMessage()));
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new DictionaryLookupFailureResponse(
                            "PROVIDER_UNAVAILABLE",
                            "Dictionary service is temporarily unavailable."));
        }
    }
}


