package com.khaleo.flashcard.service.deck;

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
import com.khaleo.flashcard.service.persistence.DeckCardAccessGuard;
import com.khaleo.flashcard.service.persistence.PersistenceValidationException;
import com.khaleo.flashcard.service.persistence.PersistenceValidationExceptionMapper;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FolderService {

    private static final int MAX_DEPTH = 6;

    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;
    private final CardLearningStateRepository cardLearningStateRepository;
    private final UserRepository userRepository;
    private final DeckCardAccessGuard deckCardAccessGuard;
    private final PersistenceValidationExceptionMapper exceptionMapper;

    @Transactional(readOnly = true)
    public List<FolderTreeResponse> getFolderTree() {
        UUID actorId = deckCardAccessGuard.requireAuthenticatedUserId("read", "folder-tree", "self");
        List<Deck> decks = deckRepository.findByAuthorId(actorId);
        return buildTree(decks, actorId);
    }

    @Transactional
    public FolderTreeResponse createFolder(String rawName, UUID parentId) {
        UUID actorId = deckCardAccessGuard.requireAuthenticatedUserId("create", "folder", "new");
        String name = rawName == null ? null : rawName.trim();
        if (name == null || name.isBlank()) {
            throw new PersistenceValidationException(
                    PersistenceValidationException.PersistenceErrorCode.VALIDATION_REJECTED,
                    "FOLDER_NAME_REQUIRED");
        }

        User author = userRepository.findById(actorId)
                .orElseThrow(() -> exceptionMapper.userNotFound(actorId));

        Deck parent = null;
        if (parentId != null) {
            parent = deckRepository.findByIdAndAuthorId(parentId, actorId)
                    .orElseThrow(() -> exceptionMapper.deckNotFound(parentId));

            int parentDepth = resolveDepth(parent);
            if (parentDepth >= MAX_DEPTH) {
                parent = parent.getParent();
            }
        }

        Deck created = deckRepository.save(Deck.builder()
                .author(author)
                .name(name)
                .isPublic(false)
                .parent(parent)
                .build());

        return FolderTreeResponse.empty(created.getId(), created.getName());
    }

    @Transactional
    public void deleteFolder(UUID folderId) {
        Deck root = deckRepository.findById(folderId)
                .orElseThrow(() -> exceptionMapper.deckNotFound(folderId));

        deckCardAccessGuard.ensureOwnerOrAdmin(root.getAuthor().getId(), "delete", "folder", folderId.toString());

        List<UUID> descendantDeckIds = collectDescendantDeckIds(folderId, root.getAuthor().getId());
        if (descendantDeckIds.isEmpty()) {
            descendantDeckIds = List.of(folderId);
        }

        List<Card> cards = cardRepository.findByDeckIdIn(descendantDeckIds);
        List<UUID> cardIds = cards.stream().map(Card::getId).toList();
        if (!cardIds.isEmpty()) {
            cardLearningStateRepository.deleteByCardIdIn(cardIds);
        }

        cardRepository.deleteByDeckIdIn(descendantDeckIds);
        deckRepository.delete(root);
    }

    @Transactional(readOnly = true)
    public List<UUID> collectDescendantDeckIds(UUID rootDeckId, UUID ownerId) {
        List<Deck> ownedDecks = deckRepository.findByAuthorId(ownerId);
        if (ownedDecks.isEmpty()) {
            return List.of(rootDeckId);
        }

        Map<UUID, List<UUID>> childrenByParent = new HashMap<>();
        Set<UUID> knownIds = new HashSet<>();
        for (Deck deck : ownedDecks) {
            knownIds.add(deck.getId());
            UUID parentId = deck.getParent() == null ? null : deck.getParent().getId();
            childrenByParent.computeIfAbsent(parentId, ignored -> new ArrayList<>()).add(deck.getId());
        }

        if (!knownIds.contains(rootDeckId)) {
            return List.of(rootDeckId);
        }

        List<UUID> ids = new ArrayList<>();
        ArrayDeque<UUID> queue = new ArrayDeque<>();
        queue.add(rootDeckId);
        while (!queue.isEmpty()) {
            UUID current = queue.removeFirst();
            ids.add(current);
            List<UUID> children = childrenByParent.getOrDefault(current, List.of());
            queue.addAll(children);
        }

        return ids;
    }

    private List<FolderTreeResponse> buildTree(List<Deck> decks, UUID actorId) {
        if (decks.isEmpty()) {
            return List.of();
        }

        Map<UUID, List<Deck>> childrenByParent = new LinkedHashMap<>();
        for (Deck deck : decks) {
            UUID parentId = deck.getParent() == null ? null : deck.getParent().getId();
            childrenByParent.computeIfAbsent(parentId, ignored -> new ArrayList<>()).add(deck);
        }

        List<UUID> deckIds = decks.stream().map(Deck::getId).toList();
        List<Card> cards = cardRepository.findByDeckIdIn(deckIds);
        Map<UUID, DeckCardStatusCounts> directCounts = computeDirectCounts(cards, actorId);

        List<Deck> roots = childrenByParent.getOrDefault(null, List.of());
        return roots.stream()
                .map(root -> buildNode(root, childrenByParent, directCounts))
                .toList();
    }

    private FolderTreeResponse buildNode(
            Deck deck,
            Map<UUID, List<Deck>> childrenByParent,
            Map<UUID, DeckCardStatusCounts> directCounts) {

        DeckCardStatusCounts direct = directCounts.getOrDefault(deck.getId(), DeckCardStatusCounts.empty());
        List<FolderTreeResponse> children = childrenByParent
                .getOrDefault(deck.getId(), List.of())
                .stream()
                .map(child -> buildNode(child, childrenByParent, directCounts))
                .toList();

        long aggNew = direct.newCards();
        long aggLearning = direct.learningCards();
        long aggMastered = direct.masteredCards();
        for (FolderTreeResponse child : children) {
            aggNew += child.newCards();
            aggLearning += child.learningCards();
            aggMastered += child.masteredCards();
        }

        return new FolderTreeResponse(
                deck.getId(),
                deck.getName(),
                aggNew + aggLearning + aggMastered,
                aggNew,
                aggLearning,
                aggMastered,
                children);
    }

    private Map<UUID, DeckCardStatusCounts> computeDirectCounts(List<Card> cards, UUID actorId) {
        if (cards.isEmpty()) {
            return Map.of();
        }

        Map<UUID, DeckCardStatusCounts> countsByDeck = new HashMap<>();
        List<UUID> cardIds = cards.stream().map(Card::getId).toList();
        List<CardLearningState> learningStates = cardLearningStateRepository.findByUserIdAndCardIdIn(actorId, cardIds);
        Map<UUID, CardLearningStateType> stateByCard = new HashMap<>();
        for (CardLearningState state : learningStates) {
            stateByCard.put(state.getCard().getId(), state.getState());
        }

        for (Card card : cards) {
            UUID deckId = card.getDeck().getId();
            DeckCardStatusCounts current = countsByDeck.getOrDefault(deckId, DeckCardStatusCounts.empty());

            CardLearningStateType state = stateByCard.get(card.getId());
            if (state == null) {
                countsByDeck.put(deckId, current.incNew());
                continue;
            }
            if (state == CardLearningStateType.MASTERED) {
                countsByDeck.put(deckId, current.incMastered());
                continue;
            }
            countsByDeck.put(deckId, current.incLearning());
        }

        return countsByDeck;
    }

    private int resolveDepth(Deck deck) {
        int depth = 1;
        Deck cursor = deck;
        Set<UUID> seen = new HashSet<>();
        while (cursor.getParent() != null) {
            UUID cursorId = cursor.getId();
            if (cursorId != null && !seen.add(cursorId)) {
                throw new PersistenceValidationException(
                        PersistenceValidationException.PersistenceErrorCode.VALIDATION_REJECTED,
                        "FOLDER_PARENT_CYCLE");
            }
            depth++;
            cursor = Objects.requireNonNull(cursor.getParent());
            if (depth > MAX_DEPTH + 1) {
                break;
            }
        }
        return depth;
    }

    private record DeckCardStatusCounts(long newCards, long learningCards, long masteredCards) {

        static DeckCardStatusCounts empty() {
            return new DeckCardStatusCounts(0, 0, 0);
        }

        DeckCardStatusCounts incNew() {
            return new DeckCardStatusCounts(newCards + 1, learningCards, masteredCards);
        }

        DeckCardStatusCounts incLearning() {
            return new DeckCardStatusCounts(newCards, learningCards + 1, masteredCards);
        }

        DeckCardStatusCounts incMastered() {
            return new DeckCardStatusCounts(newCards, learningCards, masteredCards + 1);
        }
    }
}



