package com.khaleo.flashcard.service.listening;

import java.text.Normalizer;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class DictationNormalizer {

    private static final Pattern NORMALIZE_PATTERN = Pattern.compile("[\\p{P}\\p{S}]");

    public String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll(NORMALIZE_PATTERN.pattern(), "")
                .replaceAll("\\s+", " ")
                .trim();
        return normalized.isBlank() ? null : normalized;
    }

    public boolean matchesAnyAlias(String answer, String expected, Collection<String> aliases) {
        String normalizedAnswer = normalize(answer);
        String normalizedExpected = normalize(expected);
        if (normalizedAnswer == null || normalizedExpected == null) {
            return false;
        }
        if (normalizedAnswer.equals(normalizedExpected)) {
            return true;
        }
        if (aliases == null || aliases.isEmpty()) {
            return false;
        }
        for (String alias : aliases) {
            String normalizedAlias = normalize(alias);
            if (normalizedAlias != null && normalizedAnswer.equals(normalizedAlias)) {
                return true;
            }
        }
        return false;
    }

    public Set<String> normalizeAliases(Collection<String> aliases) {
        Set<String> values = new LinkedHashSet<>();
        if (aliases == null) {
            return values;
        }
        for (String alias : aliases) {
            String normalized = normalize(alias);
            if (normalized != null) {
                values.add(normalized);
            }
        }
        return values;
    }
}


