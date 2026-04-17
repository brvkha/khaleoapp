package com.khaleo.flashcard.service.persistence;

import com.khaleo.flashcard.controller.card.dto.BulkCreateCardsRequest;
import com.khaleo.flashcard.controller.card.dto.BulkRowErrorCode;
import com.khaleo.flashcard.entity.Card;
import com.khaleo.flashcard.entity.Deck;
import com.khaleo.flashcard.repository.CardRepository;
import com.khaleo.flashcard.repository.DeckRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BulkCardImportService {

    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;
    private final DeckCardAccessGuard deckCardAccessGuard;
    private final CardHtmlSanitizer cardHtmlSanitizer;
    private final CardSearchTextBuilder cardSearchTextBuilder;
    private final PersistenceValidationExceptionMapper exceptionMapper;

    @Transactional
    public BulkCreateCardsRequest.BulkCreateCardsResponse importCards(UUID deckId, BulkCreateCardsRequest request) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> exceptionMapper.deckNotFound(deckId));
        deckCardAccessGuard.ensureOwnerOrAdmin(deck.getAuthor().getId(), "bulk-create", "card", deckId.toString());

        List<BulkCreateCardsRequest.BulkRowError> errors = new ArrayList<>();
        int successCount = 0;

        for (BulkCreateCardsRequest.BulkCreateCardItem item : request.cards()) {
            try {
                String frontContent = cardHtmlSanitizer.sanitize(item.frontContent(), "FRONT_REQUIRED");
                String backContent = cardHtmlSanitizer.sanitize(item.backContent(), "BACK_REQUIRED");
                String searchText = cardSearchTextBuilder.fromCanonicalHtml(frontContent, backContent);

                Card card = Card.builder()
                        .deck(deck)
                        .frontContent(frontContent)
                        .backContent(backContent)
                        .searchText(searchText)
                        // Keep legacy fields synchronized in phase 1 for compatibility.
                        .frontText(frontContent)
                        .backText(backContent)
                        .examplesJson("[]")
                        .build();
                cardRepository.save(card);
                successCount++;
            } catch (RuntimeException ex) {
                errors.add(new BulkCreateCardsRequest.BulkRowError(
                        item.line(),
                        exceptionMapper.mapBulkRowErrorCode(ex),
                        ex.getMessage() == null ? "Row rejected" : ex.getMessage()));
            }
        }

        int failedCount = request.cards().size() - successCount;
        return new BulkCreateCardsRequest.BulkCreateCardsResponse(successCount, failedCount, errors);
    }
}

