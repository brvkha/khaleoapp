package com.khaleo.flashcard.controller.folder.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record FolderTreeResponse(
        UUID id,
        String name,
        long totalCards,
        long newCards,
        long learningCards,
        long masteredCards,
        List<FolderTreeResponse> children) {

    public FolderTreeResponse withChildren(List<FolderTreeResponse> value) {
        return new FolderTreeResponse(
                id,
                name,
                totalCards,
                newCards,
                learningCards,
                masteredCards,
                value);
    }

    public static FolderTreeResponse empty(UUID id, String name) {
        return new FolderTreeResponse(id, name, 0, 0, 0, 0, new ArrayList<>());
    }
}

