package com.khaleo.flashcard.service.persistence;

import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CardHtmlSanitizer {

    private final Set<String> mediaAllowlistHosts;
    private final Safelist safelist;

    public CardHtmlSanitizer(
            @Value("${app.rich-card.media.allowlist-hosts:}") String mediaAllowlistHosts,
            @Value("${app.rich-card.sanitizer.allowed-tags:p,br,strong,em,u,img,audio,source,ul,ol,li,span,div}") String allowedTags,
            @Value("${app.rich-card.sanitizer.allowed-attributes:class,src,alt,controls,type}") String allowedAttributes) {
        this.mediaAllowlistHosts = Arrays.stream(mediaAllowlistHosts.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        this.safelist = Safelist.none();
        for (String tag : allowedTags.split(",")) {
            String value = tag.trim();
            if (!value.isBlank()) {
                this.safelist.addTags(value);
            }
        }

        Set<String> attrs = Arrays.stream(allowedAttributes.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
        for (String tag : new String[] {"p", "strong", "em", "u", "ul", "ol", "li", "span", "div"}) {
            if (attrs.contains("class")) {
                this.safelist.addAttributes(tag, "class");
            }
        }
        if (attrs.contains("src")) {
            this.safelist.addAttributes("img", "src");
            this.safelist.addAttributes("audio", "src");
            this.safelist.addAttributes("source", "src");
            this.safelist.addProtocols("img", "src", "https");
            this.safelist.addProtocols("audio", "src", "https");
            this.safelist.addProtocols("source", "src", "https");
        }
        if (attrs.contains("alt")) {
            this.safelist.addAttributes("img", "alt");
        }
        if (attrs.contains("controls")) {
            this.safelist.addAttributes("audio", "controls");
        }
        if (attrs.contains("type")) {
            this.safelist.addAttributes("source", "type");
        }
    }

    public String sanitize(String html, String requiredCode) {
        if (html == null || html.isBlank()) {
            throw new PersistenceValidationException(
                    PersistenceValidationException.PersistenceErrorCode.VALIDATION_REJECTED,
                    requiredCode);
        }

        String cleaned = Jsoup.clean(html, safelist);
        Document document = Jsoup.parseBodyFragment(cleaned);

        for (Element mediaElement : document.select("img[src],audio[src],source[src]")) {
            String src = mediaElement.attr("src");
            if (!isAllowedMediaUrl(src)) {
                throw new PersistenceValidationException(
                        PersistenceValidationException.PersistenceErrorCode.VALIDATION_REJECTED,
                        "MEDIA_DOMAIN_NOT_ALLOWED");
            }
        }

        String normalized = document.body().html().trim();
        if (normalized.isBlank()) {
            throw new PersistenceValidationException(
                    PersistenceValidationException.PersistenceErrorCode.VALIDATION_REJECTED,
                    "HTML_UNRECOVERABLE");
        }
        return normalized;
    }

    private boolean isAllowedMediaUrl(String url) {
        try {
            URI uri = URI.create(url);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
                return false;
            }
            if (mediaAllowlistHosts.isEmpty()) {
                return true;
            }
            return mediaAllowlistHosts.contains(uri.getHost().toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}


