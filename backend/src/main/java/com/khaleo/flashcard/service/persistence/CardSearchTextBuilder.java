package com.khaleo.flashcard.service.persistence;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

@Component
public class CardSearchTextBuilder {

    public String fromCanonicalHtml(String frontContent, String backContent) {
        String front = toText(frontContent);
        String back = toText(backContent);
        String combined = (front + " " + back).trim().replaceAll("\\s+", " ");
        return combined.toLowerCase();
    }

    private String toText(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        return Jsoup.parseBodyFragment(html).text();
    }
}

