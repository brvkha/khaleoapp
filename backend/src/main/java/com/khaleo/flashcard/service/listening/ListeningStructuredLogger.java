package com.khaleo.flashcard.service.listening;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ListeningStructuredLogger {

    public void info(String event, Map<String, Object> attributes) {
        log.info("event={} attributes={}", event, attributes);
    }

    public void warn(String event, Map<String, Object> attributes) {
        log.warn("event={} attributes={}", event, attributes);
    }
}