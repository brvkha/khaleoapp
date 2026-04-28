package com.khaleo.flashcard.service.listening;

import com.khaleo.flashcard.controller.listening.dto.MediaAccessResponse;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ListeningMediaAccessService {

    private static final long MIN_TTL_SECONDS = 60;
    private static final long MAX_TTL_SECONDS = 300;

    private final long presignedUrlTtlSeconds;
    private final ListeningStructuredLogger listeningStructuredLogger;

    public ListeningMediaAccessService(
            @Value("${app.listening.media.presigned-url-ttl-seconds:300}") long presignedUrlTtlSeconds,
            ListeningStructuredLogger listeningStructuredLogger) {
        if (presignedUrlTtlSeconds < MIN_TTL_SECONDS || presignedUrlTtlSeconds > MAX_TTL_SECONDS) {
            throw new IllegalStateException("Listening media presigned URL TTL must be between 60 and 300 seconds.");
        }
        this.presignedUrlTtlSeconds = presignedUrlTtlSeconds;
        this.listeningStructuredLogger = listeningStructuredLogger;
    }

    public MediaAccessResponse issueAccessUrl(String mediaUrl) {
        if (mediaUrl == null || mediaUrl.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mediaUrl is required.");
        }
        String trimmedMediaUrl = mediaUrl.trim();
        listeningStructuredLogger.info("listening_media_access_issued", java.util.Map.of(
                "mediaUrl", trimmedMediaUrl,
                "ttlSeconds", presignedUrlTtlSeconds
        ));
        return new MediaAccessResponse(trimmedMediaUrl, Instant.now().plusSeconds(presignedUrlTtlSeconds));
    }
}

