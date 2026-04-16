package com.khaleo.flashcard.controller.folder;

import com.khaleo.flashcard.controller.folder.dto.FolderCreateRequest;
import com.khaleo.flashcard.controller.folder.dto.FolderTreeResponse;
import com.khaleo.flashcard.service.auth.VerifiedAccountGuard;
import com.khaleo.flashcard.service.deck.FolderService;
import com.khaleo.flashcard.service.persistence.DeckCardAccessGuard;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;
    private final DeckCardAccessGuard deckCardAccessGuard;
    private final VerifiedAccountGuard verifiedAccountGuard;

    @GetMapping("/tree")
    public List<FolderTreeResponse> getFolderTree() {
        UUID actorId = deckCardAccessGuard.requireAuthenticatedUserId("read", "folder", "tree");
        verifiedAccountGuard.requireVerified(actorId, "read", "folder", "tree");
        return folderService.getFolderTree();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FolderTreeResponse createFolder(@Valid @RequestBody FolderCreateRequest request) {
        UUID actorId = deckCardAccessGuard.requireAuthenticatedUserId("create", "folder", "new");
        verifiedAccountGuard.requireVerified(actorId, "create", "folder", "new");
        return folderService.createFolder(request.name(), request.parentId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFolder(@PathVariable("id") UUID id) {
        UUID actorId = deckCardAccessGuard.requireAuthenticatedUserId("delete", "folder", id.toString());
        verifiedAccountGuard.requireVerified(actorId, "delete", "folder", id.toString());
        folderService.deleteFolder(id);
    }
}

