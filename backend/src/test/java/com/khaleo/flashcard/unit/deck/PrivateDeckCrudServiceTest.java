package com.khaleo.flashcard.unit.deck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.khaleo.flashcard.entity.Card;
import com.khaleo.flashcard.entity.Deck;
import com.khaleo.flashcard.service.deck.DeckAuthorizationService;
import com.khaleo.flashcard.service.deck.PrivateDeckCrudService;
import com.khaleo.flashcard.service.persistence.RelationalPersistenceService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

class PrivateDeckCrudServiceTest {

    private DeckAuthorizationService deckAuthorizationService;
    private RelationalPersistenceService relationalPersistenceService;
    private PrivateDeckCrudService privateDeckCrudService;

    @BeforeEach
    void setUp() {
        deckAuthorizationService = Mockito.mock(DeckAuthorizationService.class);
        relationalPersistenceService = Mockito.mock(RelationalPersistenceService.class);
        privateDeckCrudService = new PrivateDeckCrudService(deckAuthorizationService, relationalPersistenceService);
    }

    @Test
    void shouldForcePrivateFlagWhenCreatingDeck() {
        UUID actorId = UUID.randomUUID();
        RelationalPersistenceService.CreateDeckRequest request =
                new RelationalPersistenceService.CreateDeckRequest("Deck A", "Desc", "https://cdn/img.png", true, "tag-a");

        when(relationalPersistenceService.createDeck(eq(actorId), any())).thenAnswer(invocation -> {
            RelationalPersistenceService.CreateDeckRequest persisted = invocation.getArgument(1);
            return Deck.builder()
                    .id(UUID.randomUUID())
                    .name(persisted.name())
                    .description(persisted.description())
                    .coverImageUrl(persisted.coverImageUrl())
                    .isPublic(persisted.isPublic())
                    .tags(persisted.tags())
                    .build();
        });

        Deck saved = privateDeckCrudService.createPrivateDeck(actorId, request);

        ArgumentCaptor<RelationalPersistenceService.CreateDeckRequest> captor =
                ArgumentCaptor.forClass(RelationalPersistenceService.CreateDeckRequest.class);
        verify(relationalPersistenceService).createDeck(eq(actorId), captor.capture());
        assertThat(captor.getValue().isPublic()).isFalse();
        assertThat(saved.getIsPublic()).isFalse();
    }

    @Test
    void shouldCheckOwnershipBeforeUpdatingDeck() {
        UUID actorId = UUID.randomUUID();
        UUID deckId = UUID.randomUUID();
        RelationalPersistenceService.UpdateDeckRequest request =
                new RelationalPersistenceService.UpdateDeckRequest("New Name", "Desc", null, false, "tag");

        privateDeckCrudService.updatePrivateDeck(actorId, deckId, request);

        verify(deckAuthorizationService).requireOwnedPrivateDeck(actorId, deckId, "update");
        verify(relationalPersistenceService).updateDeck(deckId, request);
    }

    @Test
    void shouldCheckOwnershipBeforeDeletingDeck() {
        UUID actorId = UUID.randomUUID();
        UUID deckId = UUID.randomUUID();

        privateDeckCrudService.deletePrivateDeck(actorId, deckId);

        verify(deckAuthorizationService).requireOwnedPrivateDeck(actorId, deckId, "delete");
        verify(relationalPersistenceService).deleteDeck(deckId);
    }

    @Test
    void shouldCheckOwnershipBeforeSearchingCards() {
        UUID actorId = UUID.randomUUID();
        UUID deckId = UUID.randomUUID();
        RelationalPersistenceService.CardSearchQuery query =
                new RelationalPersistenceService.CardSearchQuery("term", "answer", null, 0, 10);
        Page<Card> expected = new PageImpl<>(List.of(Card.builder().id(UUID.randomUUID()).build()));

        when(relationalPersistenceService.searchCardsInDeck(deckId, query)).thenReturn(expected);

        Page<Card> actual = privateDeckCrudService.searchCards(actorId, deckId, query);

        verify(deckAuthorizationService).requireOwnedPrivateDeck(actorId, deckId, "search");
        assertThat(actual).isSameAs(expected);
    }
}

