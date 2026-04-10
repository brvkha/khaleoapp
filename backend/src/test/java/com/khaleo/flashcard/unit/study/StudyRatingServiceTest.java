package com.khaleo.flashcard.unit.study;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.khaleo.flashcard.config.observability.NewRelicDeckMediaInstrumentation;
import com.khaleo.flashcard.entity.Card;
import com.khaleo.flashcard.entity.CardLearningState;
import com.khaleo.flashcard.entity.Deck;
import com.khaleo.flashcard.entity.User;
import com.khaleo.flashcard.entity.enums.CardLearningStateType;
import com.khaleo.flashcard.model.dynamo.RatingGiven;
import com.khaleo.flashcard.model.study.RateCardRequest;
import com.khaleo.flashcard.model.study.RateCardResponse;
import com.khaleo.flashcard.repository.CardLearningStateRepository;
import com.khaleo.flashcard.repository.UserRepository;
import com.khaleo.flashcard.service.activitylog.StudyActivityLogPublisher;
import com.khaleo.flashcard.service.persistence.CardLearningStateUpdateService;
import com.khaleo.flashcard.service.persistence.PersistenceValidationExceptionMapper;
import com.khaleo.flashcard.service.study.SpacedRepetitionService;
import com.khaleo.flashcard.service.study.StudyAccessService;
import com.khaleo.flashcard.service.study.StudyRatingService;
import com.khaleo.flashcard.service.study.StudySchedulerService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class StudyRatingServiceTest {

    private StudyAccessService studyAccessService;
    private CardLearningStateRepository cardLearningStateRepository;
    private CardLearningStateUpdateService cardLearningStateUpdateService;
    private UserRepository userRepository;
    private StudySchedulerService studySchedulerService;
    private StudyActivityLogPublisher studyActivityLogPublisher;
    private StudyRatingService studyRatingService;

    @BeforeEach
    void setUp() {
        studyAccessService = Mockito.mock(StudyAccessService.class);
        cardLearningStateRepository = Mockito.mock(CardLearningStateRepository.class);
        cardLearningStateUpdateService = Mockito.mock(CardLearningStateUpdateService.class);
        userRepository = Mockito.mock(UserRepository.class);
        studySchedulerService = Mockito.mock(StudySchedulerService.class);
        studyActivityLogPublisher = Mockito.mock(StudyActivityLogPublisher.class);
        PersistenceValidationExceptionMapper exceptionMapper = Mockito.mock(PersistenceValidationExceptionMapper.class);
        NewRelicDeckMediaInstrumentation instrumentation = Mockito.mock(NewRelicDeckMediaInstrumentation.class);

        studyRatingService = new StudyRatingService(
                studyAccessService,
                cardLearningStateRepository,
                cardLearningStateUpdateService,
                userRepository,
                studySchedulerService,
                studyActivityLogPublisher,
                exceptionMapper,
                instrumentation);
    }

    @Test
    void shouldRejectNullRating() {
        UUID cardId = UUID.randomUUID();
        mockCardAccess(cardId);

        assertThatThrownBy(() -> studyRatingService.rateCard(cardId, new RateCardRequest(null, 100L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rating is required");

        verify(cardLearningStateRepository, never()).saveAndFlush(any(CardLearningState.class));
    }

    @Test
    void shouldRejectNegativeTimeSpent() {
        UUID cardId = UUID.randomUUID();
        mockCardAccess(cardId);

        assertThatThrownBy(() -> studyRatingService.rateCard(cardId, new RateCardRequest(RatingGiven.GOOD, -1L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("timeSpentMs must be non-negative");

        verify(cardLearningStateRepository, never()).saveAndFlush(any(CardLearningState.class));
    }

    @Test
    void shouldPersistRatedOutcomeAndPublishActivity() {
        UUID cardId = UUID.randomUUID();
        UUID deckId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Deck deck = Deck.builder().id(deckId).build();
        Card card = Card.builder().id(cardId).deck(deck).build();
        User user = User.builder().id(actorId).build();

        when(studyAccessService.requireCardAccess(cardId))
                .thenReturn(new StudyAccessService.CardAccessContext(actorId, card, deck));
        when(cardLearningStateRepository.findByUserIdAndCardId(actorId, cardId)).thenReturn(Optional.empty());
        when(userRepository.findById(actorId)).thenReturn(Optional.of(user));

        Instant nextReview = Instant.parse("2026-04-11T10:15:30Z");
        SpacedRepetitionService.RatingOutcome outcome = new SpacedRepetitionService.RatingOutcome(
                CardLearningStateType.LEARNING,
                nextReview,
                1,
                BigDecimal.valueOf(2.34),
                BigDecimal.valueOf(4.56),
                0,
                1,
                0,
                nextReview.minusSeconds(30));
        when(studySchedulerService.apply(any(CardLearningState.class), eq(RatingGiven.GOOD), any(Instant.class)))
                .thenReturn(outcome);

        when(cardLearningStateUpdateService.saveWithSingleRetry(eq(actorId), eq(cardId), any()))
                .thenAnswer(invocation -> {
                    Supplier<CardLearningState> saveAttempt = invocation.getArgument(2);
                    return saveAttempt.get();
                });
        when(cardLearningStateRepository.saveAndFlush(any(CardLearningState.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RateCardResponse response = studyRatingService.rateCard(cardId, new RateCardRequest(RatingGiven.GOOD, 1234L));

        verify(studyAccessService).requireCardAccess(cardId);
        verify(cardLearningStateRepository).findByUserIdAndCardId(actorId, cardId);
        verify(cardLearningStateRepository).saveAndFlush(any(CardLearningState.class));
        verify(studyActivityLogPublisher)
                .publishRatingEvent(actorId, cardId, deckId, RatingGiven.GOOD, 1234L, 1, BigDecimal.valueOf(2.34), BigDecimal.valueOf(4.56));
        assertThat(response.cardId()).isEqualTo(cardId);
        assertThat(response.state()).isEqualTo(CardLearningStateType.LEARNING);
        assertThat(response.nextReviewAt()).isEqualTo(nextReview);
    }

    private void mockCardAccess(UUID cardId) {
        Deck deck = Deck.builder().id(UUID.randomUUID()).build();
        Card card = Card.builder().id(cardId).deck(deck).build();
        when(studyAccessService.requireCardAccess(cardId))
                .thenReturn(new StudyAccessService.CardAccessContext(UUID.randomUUID(), card, deck));
    }
}

