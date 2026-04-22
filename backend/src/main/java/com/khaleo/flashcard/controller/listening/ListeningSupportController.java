package com.khaleo.flashcard.controller.listening;

import com.khaleo.flashcard.controller.listening.dto.DictionaryLookupFailureResponse;
import com.khaleo.flashcard.controller.listening.dto.DictionaryLookupResponse;
import com.khaleo.flashcard.controller.listening.dto.MediaAccessRequest;
import com.khaleo.flashcard.controller.listening.dto.MediaAccessResponse;
import com.khaleo.flashcard.service.listening.DictionaryProxyService;
import com.khaleo.flashcard.service.listening.ListeningMediaAccessService;
import jakarta.validation.Valid;
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

    @PostMapping("/media/access")
    public MediaAccessResponse issueMediaAccess(@Valid @RequestBody MediaAccessRequest request) {
        return listeningMediaAccessService.issueAccessUrl(request.mediaUrl());
    }

    @GetMapping("/dictionary")
    public ResponseEntity<?> dictionaryLookup(@RequestParam String term) {
        try {
            DictionaryLookupResponse response = dictionaryProxyService.lookup(term);
            return ResponseEntity.ok(response);
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new DictionaryLookupFailureResponse(
                            "PROVIDER_UNAVAILABLE",
                            "Dictionary service is temporarily unavailable."));
        }
    }
}


