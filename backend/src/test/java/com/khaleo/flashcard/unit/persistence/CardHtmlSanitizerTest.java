package com.khaleo.flashcard.unit.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.khaleo.flashcard.service.persistence.CardHtmlSanitizer;
import com.khaleo.flashcard.service.persistence.PersistenceValidationException;
import org.junit.jupiter.api.Test;

class CardHtmlSanitizerTest {

    private final CardHtmlSanitizer sanitizer = new CardHtmlSanitizer(
            "images.unsplash.com",
            "p,br,strong,em,u,img,audio,source,ul,ol,li,span,div",
            "class,src,alt,controls,type");

    @Test
    void stripsDisallowedTagsAndKeepsAllowedMarkup() {
        String sanitized = sanitizer.sanitize("<p>Hello</p><script>alert(1)</script>", "FRONT_REQUIRED");
        assertThat(sanitized).contains("<p>Hello</p>");
        assertThat(sanitized).doesNotContain("script");
    }

    @Test
    void rejectsMediaOutsideAllowlist() {
        assertThatThrownBy(() -> sanitizer.sanitize("<img src=\"https://evil.example/img.png\" />", "FRONT_REQUIRED"))
                .isInstanceOf(PersistenceValidationException.class)
                .hasMessageContaining("MEDIA_DOMAIN_NOT_ALLOWED");
    }
}

