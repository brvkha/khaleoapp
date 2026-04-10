package com.khaleo.flashcard.unit.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.khaleo.flashcard.config.FeatureTelemetryLogger;
import com.khaleo.flashcard.config.observability.NewRelicDeckMediaInstrumentation;
import com.khaleo.flashcard.entity.Card;
import com.khaleo.flashcard.entity.Deck;
import com.khaleo.flashcard.entity.User;
import com.khaleo.flashcard.repository.CardLearningStateRepository;
import com.khaleo.flashcard.repository.CardRepository;
import com.khaleo.flashcard.repository.DeckImportLinkRepository;
import com.khaleo.flashcard.repository.DeckRepository;
import com.khaleo.flashcard.repository.ReimportMergeConflictRepository;
import com.khaleo.flashcard.repository.UserRepository;
import com.khaleo.flashcard.service.activitylog.StudyActivityLogPublisher;
import com.khaleo.flashcard.service.media.MediaReferenceService;
import com.khaleo.flashcard.service.persistence.CardLearningStateUpdateService;
import com.khaleo.flashcard.service.persistence.DeckCardAccessGuard;
import com.khaleo.flashcard.service.persistence.PersistenceValidationException;
import com.khaleo.flashcard.service.persistence.PersistenceValidationExceptionMapper;
import com.khaleo.flashcard.service.persistence.RelationalPersistenceService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class RelationalPersistenceCardServiceTest {

    private DeckRepository deckRepository;
    private CardRepository cardRepository;
    private DeckCardAccessGuard deckCardAccessGuard;
    private MediaReferenceService mediaReferenceService;
    private RelationalPersistenceService relationalPersistenceService;

    @BeforeEach
    void setUp() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        deckRepository = Mockito.mock(DeckRepository.class);
        cardRepository = Mockito.mock(CardRepository.class);
        CardLearningStateRepository cardLearningStateRepository = Mockito.mock(CardLearningStateRepository.class);
        DeckImportLinkRepository deckImportLinkRepository = Mockito.mock(DeckImportLinkRepository.class);
        ReimportMergeConflictRepository reimportMergeConflictRepository = Mockito.mock(ReimportMergeConflictRepository.class);
        StudyActivityLogPublisher studyActivityLogPublisher = Mockito.mock(StudyActivityLogPublisher.class);
        PersistenceValidationExceptionMapper exceptionMapper = new PersistenceValidationExceptionMapper();
        CardLearningStateUpdateService cardLearningStateUpdateService = Mockito.mock(CardLearningStateUpdateService.class);
        deckCardAccessGuard = Mockito.mock(DeckCardAccessGuard.class);
        mediaReferenceService = Mockito.mock(MediaReferenceService.class);
        NewRelicDeckMediaInstrumentation instrumentation = Mockito.mock(NewRelicDeckMediaInstrumentation.class);
        FeatureTelemetryLogger telemetryLogger = Mockito.mock(FeatureTelemetryLogger.class);

        relationalPersistenceService = new RelationalPersistenceService(
                userRepository,
                deckRepository,
                cardRepository,
                cardLearningStateRepository,
                deckImportLinkRepository,
                reimportMergeConflictRepository,
                studyActivityLogPublisher,
                exceptionMapper,
                cardLearningStateUpdateService,
                deckCardAccessGuard,
                mediaReferenceService,
                instrumentation,
                telemetryLogger);

        ReflectionTestUtils.setField(relationalPersistenceService, "imageAllowlistHosts", "");
        ReflectionTestUtils.setField(relationalPersistenceService, "maxExamples", 20);
        ReflectionTestUtils.setField(relationalPersistenceService, "maxExampleLength", 300);
        ReflectionTestUtils.setField(relationalPersistenceService, "maxPayloadBytes", 65536);
    }

    @Test
    void shouldEnforceDeckOwnershipBeforeCreatingCard() {
        UUID deckId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        Deck deck = Deck.builder().id(deckId).author(User.builder().id(authorId).build()).build();
        RelationalPersistenceService.CreateCardRequest request =
                new RelationalPersistenceService.CreateCardRequest("Term", "Answer", null, null, null, List.of("Example"));

        when(deckRepository.findById(deckId)).thenReturn(java.util.Optional.of(deck));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card toSave = invocation.getArgument(0);
            toSave.setId(UUID.randomUUID());
            return toSave;
        });

        Card saved = relationalPersistenceService.createCard(deckId, request);

        verify(deckCardAccessGuard).ensureOwnerOrAdmin(authorId, "create", "card", deckId.toString());
        verify(cardRepository).save(any(Card.class));
        assertThat(saved.getTerm()).isEqualTo("Term");
        assertThat(saved.getAnswer()).isEqualTo("Answer");
    }

    @Test
    void shouldRejectCreateCardWhenTermIsBlank() {
        UUID deckId = UUID.randomUUID();
        Deck deck = Deck.builder().id(deckId).author(User.builder().id(UUID.randomUUID()).build()).build();
        RelationalPersistenceService.CreateCardRequest request =
                new RelationalPersistenceService.CreateCardRequest("   ", "Answer", null, null, null, List.of());

        when(deckRepository.findById(deckId)).thenReturn(java.util.Optional.of(deck));

        assertThatThrownBy(() -> relationalPersistenceService.createCard(deckId, request))
                .isInstanceOf(PersistenceValidationException.class)
                .satisfies(ex -> assertThat(((PersistenceValidationException) ex).getErrorCode())
                        .isEqualTo(PersistenceValidationException.PersistenceErrorCode.VALIDATION_REJECTED));

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void shouldRejectCreateCardWhenAnswerIsBlank() {
        UUID deckId = UUID.randomUUID();
        Deck deck = Deck.builder().id(deckId).author(User.builder().id(UUID.randomUUID()).build()).build();
        RelationalPersistenceService.CreateCardRequest request =
                new RelationalPersistenceService.CreateCardRequest("Term", "", null, null, null, List.of());

        when(deckRepository.findById(deckId)).thenReturn(java.util.Optional.of(deck));

        assertThatThrownBy(() -> relationalPersistenceService.createCard(deckId, request))
                .isInstanceOf(PersistenceValidationException.class)
                .satisfies(ex -> assertThat(((PersistenceValidationException) ex).getErrorCode())
                        .isEqualTo(PersistenceValidationException.PersistenceErrorCode.VALIDATION_REJECTED));

        verify(cardRepository, never()).save(any(Card.class));
        verify(deckCardAccessGuard).ensureOwnerOrAdmin(eq(deck.getAuthor().getId()), eq("create"), eq("card"), eq(deckId.toString()));
    }
}

