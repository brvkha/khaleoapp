package com.khaleo.flashcard.service.listening;

import com.khaleo.flashcard.controller.listening.dto.MediaAccessResponse;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ListeningMediaAccessService {

    private final long presignedUrlTtlSeconds;

    public ListeningMediaAccessService(
            @Value("${app.listening.media.presigned-url-ttl-seconds:300}") long presignedUrlTtlSeconds) {
        this.presignedUrlTtlSeconds = presignedUrlTtlSeconds;
    }

    public MediaAccessResponse issueAccessUrl(String mediaUrl) {
        return new MediaAccessResponse(mediaUrl, Instant.now().plusSeconds(presignedUrlTtlSeconds));
    }
}

