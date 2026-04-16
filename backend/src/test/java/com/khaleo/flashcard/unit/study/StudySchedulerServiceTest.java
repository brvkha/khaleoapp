package com.khaleo.flashcard.unit.study;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.khaleo.flashcard.entity.CardLearningState;
import com.khaleo.flashcard.entity.enums.CardLearningStateType;
import com.khaleo.flashcard.model.dynamo.RatingGiven;
import com.khaleo.flashcard.service.study.SpacedRepetitionService;
import com.khaleo.flashcard.service.study.StudySchedulerService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class StudySchedulerServiceTest {

    private SpacedRepetitionService spacedRepetitionService;
    private StudySchedulerService studySchedulerService;

    @BeforeEach
    void setUp() {
        spacedRepetitionService = Mockito.mock(SpacedRepetitionService.class);
        studySchedulerService = new StudySchedulerService(spacedRepetitionService);
        ReflectionTestUtils.setField(studySchedulerService, "resetHour", 4);
    }

    @Test
    void shouldKeepShortIntervalAsRealtimeDelay() {
        Instant now = Instant.parse("2026-04-16T13:00:00Z");
        SpacedRepetitionService.RatingOutcome shortOutcome = new SpacedRepetitionService.RatingOutcome(
                CardLearningStateType.LEARNING,
                now.plusSeconds(10 * 60),
                0,
                BigDecimal.ONE,
                BigDecimal.ONE,
                0,
                1,
                0,
                now);
        when(spacedRepetitionService.apply(any(CardLearningState.class), eq(RatingGiven.GOOD), eq(now)))
                .thenReturn(shortOutcome);

        SpacedRepetitionService.RatingOutcome outcome = studySchedulerService.apply(
                CardLearningState.builder().state(CardLearningStateType.NEW).build(),
                RatingGiven.GOOD,
                now,
                ZoneId.of("Asia/Ho_Chi_Minh"));

        assertThat(outcome.state()).isEqualTo(CardLearningStateType.LEARNING);
        assertThat(outcome.nextReviewAt()).isEqualTo(now.plusSeconds(10 * 60));
        assertThat(outcome.scheduledDays()).isZero();
    }

    @Test
    void shouldMapOneDayIntervalToNextResetAndMastered() {
        Instant now = Instant.parse("2026-04-16T13:00:00Z"); // 20:00 Asia/Ho_Chi_Minh
        SpacedRepetitionService.RatingOutcome longOutcome = new SpacedRepetitionService.RatingOutcome(
                CardLearningStateType.REVIEW,
                now.plusSeconds(24 * 60 * 60),
                1,
                BigDecimal.ONE,
                BigDecimal.ONE,
                0,
                1,
                0,
                now);
        when(spacedRepetitionService.apply(any(CardLearningState.class), eq(RatingGiven.GOOD), eq(now)))
                .thenReturn(longOutcome);

        SpacedRepetitionService.RatingOutcome outcome = studySchedulerService.apply(
                CardLearningState.builder().state(CardLearningStateType.REVIEW).build(),
                RatingGiven.GOOD,
                now,
                ZoneId.of("Asia/Ho_Chi_Minh"));

        assertThat(outcome.state()).isEqualTo(CardLearningStateType.MASTERED);
        assertThat(outcome.nextReviewAt()).isEqualTo(Instant.parse("2026-04-16T21:00:00Z")); // 04:00 next day ICT
        assertThat(outcome.scheduledDays()).isEqualTo(1);
    }

    @Test
    void shouldMapTwoDayIntervalToSecondResetAndMastered() {
        Instant now = Instant.parse("2026-04-16T13:00:00Z"); // 20:00 ICT
        SpacedRepetitionService.RatingOutcome longOutcome = new SpacedRepetitionService.RatingOutcome(
                CardLearningStateType.REVIEW,
                now.plusSeconds(48 * 60 * 60),
                2,
                BigDecimal.ONE,
                BigDecimal.ONE,
                0,
                1,
                0,
                now);
        when(spacedRepetitionService.apply(any(CardLearningState.class), eq(RatingGiven.GOOD), eq(now)))
                .thenReturn(longOutcome);

        SpacedRepetitionService.RatingOutcome outcome = studySchedulerService.apply(
                CardLearningState.builder().state(CardLearningStateType.REVIEW).build(),
                RatingGiven.GOOD,
                now,
                ZoneId.of("Asia/Ho_Chi_Minh"));

        assertThat(outcome.state()).isEqualTo(CardLearningStateType.MASTERED);
        assertThat(outcome.nextReviewAt()).isEqualTo(Instant.parse("2026-04-17T21:00:00Z")); // 04:00 day+2 ICT
        assertThat(outcome.scheduledDays()).isEqualTo(2);
    }
}

