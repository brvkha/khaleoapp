package com.khaleo.flashcard.unit.deck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.khaleo.flashcard.controller.folder.dto.FolderTreeResponse;
import com.khaleo.flashcard.entity.Card;
import com.khaleo.flashcard.entity.CardLearningState;
import com.khaleo.flashcard.entity.Deck;
import com.khaleo.flashcard.entity.User;
import com.khaleo.flashcard.entity.enums.CardLearningStateType;
import com.khaleo.flashcard.repository.CardLearningStateRepository;
import com.khaleo.flashcard.repository.CardRepository;
import com.khaleo.flashcard.repository.DeckRepository;
import com.khaleo.flashcard.repository.UserRepository;
import com.khaleo.flashcard.service.deck.FolderService;
import com.khaleo.flashcard.service.persistence.DeckCardAccessGuard;
import com.khaleo.flashcard.service.persistence.PersistenceValidationExceptionMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class FolderServiceTest {

    private DeckRepository deckRepository;
    private CardRepository cardRepository;
    private CardLearningStateRepository cardLearningStateRepository;
    private UserRepository userRepository;
    private DeckCardAccessGuard deckCardAccessGuard;
    private PersistenceValidationExceptionMapper exceptionMapper;
    private FolderService folderService;

    @BeforeEach
    void setUp() {
        deckRepository = Mockito.mock(DeckRepository.class);
        cardRepository = Mockito.mock(CardRepository.class);
        cardLearningStateRepository = Mockito.mock(CardLearningStateRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        deckCardAccessGuard = Mockito.mock(DeckCardAccessGuard.class);
        exceptionMapper = Mockito.mock(PersistenceValidationExceptionMapper.class);

        folderService = new FolderService(
                deckRepository,
                cardRepository,
                cardLearningStateRepository,
                userRepository,
                deckCardAccessGuard,
                exceptionMapper);
    }

    @Test
    void createFolderShouldFallbackToSiblingAtMaxDepth() {
        UUID actorId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        User author = User.builder().id(actorId).email("user@example.com").passwordHash("hash").build();

        Deck level5 = Deck.builder().id(UUID.randomUUID()).name("Level 5").author(author).build();
        Deck level6 = Deck.builder().id(parentId).name("Level 6").author(author).parent(level5).build();

        // Build a depth of 6 for the parent by chaining parents.
        Deck level4 = Deck.builder().id(UUID.randomUUID()).author(author).build();
        Deck level3 = Deck.builder().id(UUID.randomUUID()).author(author).build();
        Deck level2 = Deck.builder().id(UUID.randomUUID()).author(author).build();
        Deck level1 = Deck.builder().id(UUID.randomUUID()).author(author).build();
        level5.setParent(level4);
        level4.setParent(level3);
        level3.setParent(level2);
        level2.setParent(level1);

        when(deckCardAccessGuard.requireAuthenticatedUserId("create", "folder", "new")).thenReturn(actorId);
        when(userRepository.findById(actorId)).thenReturn(Optional.of(author));
        when(deckRepository.findByIdAndAuthorId(parentId, actorId)).thenReturn(Optional.of(level6));
        when(deckRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        folderService.createFolder("Child", parentId);

        ArgumentCaptor<Deck> captor = ArgumentCaptor.forClass(Deck.class);
        verify(deckRepository).save(captor.capture());
        assertThat(captor.getValue().getParent()).isEqualTo(level5);
    }

    @Test
    void getFolderTreeShouldAggregateDescendantCounts() {
        UUID actorId = UUID.randomUUID();
        User author = User.builder().id(actorId).email("user@example.com").passwordHash("hash").build();

        Deck root = Deck.builder().id(UUID.randomUUID()).name("Root").author(author).build();
        Deck child = Deck.builder().id(UUID.randomUUID()).name("Child").author(author).parent(root).build();

        Card rootCard = Card.builder().id(UUID.randomUUID()).deck(root).build();
        Card childCard = Card.builder().id(UUID.randomUUID()).deck(child).build();

        CardLearningState masteredState = CardLearningState.builder()
                .id(UUID.randomUUID())
                .card(childCard)
                .user(author)
                .state(CardLearningStateType.MASTERED)
                .build();

        when(deckCardAccessGuard.requireAuthenticatedUserId("read", "folder-tree", "self")).thenReturn(actorId);
        when(deckRepository.findByAuthorId(actorId)).thenReturn(List.of(root, child));
        when(cardRepository.findByDeckIdIn(List.of(root.getId(), child.getId()))).thenReturn(List.of(rootCard, childCard));
        when(cardLearningStateRepository.findByUserIdAndCardIdIn(eq(actorId), any())).thenReturn(List.of(masteredState));

        List<FolderTreeResponse> tree = folderService.getFolderTree();

        assertThat(tree).hasSize(1);
        FolderTreeResponse rootNode = tree.get(0);
        assertThat(rootNode.totalCards()).isEqualTo(2);
        assertThat(rootNode.newCards()).isEqualTo(1);
        assertThat(rootNode.masteredCards()).isEqualTo(1);
        assertThat(rootNode.children()).hasSize(1);
        assertThat(rootNode.children().get(0).masteredCards()).isEqualTo(1);
    }

    @Test
    void getFolderTreeShouldNotCountFutureMasteredAsReviewDue() {
        UUID actorId = UUID.randomUUID();
        User author = User.builder().id(actorId).email("user@example.com").passwordHash("hash").build();

        Deck root = Deck.builder().id(UUID.randomUUID()).name("Root").author(author).build();
        Card rootCard = Card.builder().id(UUID.randomUUID()).deck(root).build();

        CardLearningState futureMastered = CardLearningState.builder()
                .id(UUID.randomUUID())
                .card(rootCard)
                .user(author)
                .state(CardLearningStateType.MASTERED)
                .nextReviewDate(Instant.now().plusSeconds(3600))
                .build();

        when(deckCardAccessGuard.requireAuthenticatedUserId("read", "folder-tree", "self")).thenReturn(actorId);
        when(deckRepository.findByAuthorId(actorId)).thenReturn(List.of(root));
        when(cardRepository.findByDeckIdIn(List.of(root.getId()))).thenReturn(List.of(rootCard));
        when(cardLearningStateRepository.findByUserIdAndCardIdIn(eq(actorId), any())).thenReturn(List.of(futureMastered));

        List<FolderTreeResponse> tree = folderService.getFolderTree();

        assertThat(tree).hasSize(1);
        FolderTreeResponse rootNode = tree.get(0);
        assertThat(rootNode.masteredCards()).isEqualTo(0);
        assertThat(rootNode.learningCards()).isEqualTo(0);
        assertThat(rootNode.newCards()).isEqualTo(0);
        assertThat(rootNode.totalCards()).isEqualTo(0);
    }

    @Test
    void deleteFolderShouldDeleteCardsAcrossDescendantsBeforeRootDelete() {
        UUID ownerId = UUID.randomUUID();
        UUID rootId = UUID.randomUUID();
        UUID childId = UUID.randomUUID();

        User owner = User.builder().id(ownerId).email("owner@example.com").passwordHash("hash").build();
        Deck root = Deck.builder().id(rootId).name("Root").author(owner).build();
        Deck child = Deck.builder().id(childId).name("Child").author(owner).parent(root).build();

        Card cardInRoot = Card.builder().id(UUID.randomUUID()).deck(root).build();
        Card cardInChild = Card.builder().id(UUID.randomUUID()).deck(child).build();

        when(deckRepository.findById(rootId)).thenReturn(Optional.of(root));
        when(deckRepository.findByAuthorId(ownerId)).thenReturn(List.of(root, child));
        when(cardRepository.findByDeckIdIn(List.of(rootId, childId))).thenReturn(List.of(cardInRoot, cardInChild));

        folderService.deleteFolder(rootId);

        verify(cardLearningStateRepository).deleteByCardIdIn(List.of(cardInRoot.getId(), cardInChild.getId()));
        verify(cardRepository).deleteByDeckIdIn(List.of(rootId, childId));
        verify(deckRepository).delete(root);
    }
}

