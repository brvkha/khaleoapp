package com.khaleo.flashcard.service.study;

import com.khaleo.flashcard.entity.CardLearningState;
import com.khaleo.flashcard.entity.enums.CardLearningStateType;
import com.khaleo.flashcard.model.dynamo.RatingGiven;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudySchedulerService {

    private final SpacedRepetitionService spacedRepetitionService;

    @Value("${app.study.reset-hour:4}")
    private int resetHour;

    public SpacedRepetitionService.RatingOutcome apply(
            CardLearningState currentState,
            RatingGiven rating,
            Instant now,
            ZoneId userZone) {
        SpacedRepetitionService.RatingOutcome raw = spacedRepetitionService.apply(currentState, rating, now);
        if (raw.scheduledDays() == null || raw.scheduledDays() < 1) {
            return raw;
        }

        Instant nextResetDue = toResetCycleDue(now, userZone, raw.scheduledDays());
        return new SpacedRepetitionService.RatingOutcome(
                CardLearningStateType.MASTERED,
                nextResetDue,
                raw.scheduledDays(),
                raw.stability(),
                raw.difficulty(),
                raw.elapsedDays(),
                raw.reps(),
                raw.lapses(),
                raw.lastReviewedAt());
    }

    public SpacedRepetitionService.RatingOutcome apply(CardLearningState currentState, RatingGiven rating, Instant now) {
        return apply(currentState, rating, now, ZoneId.of("Asia/Ho_Chi_Minh"));
    }

    private Instant toResetCycleDue(Instant now, ZoneId zoneId, int cycleCount) {
        ZonedDateTime localNow = now.atZone(zoneId);
        ZonedDateTime nextReset = localNow.toLocalDate().atTime(resetHour, 0).atZone(zoneId);
        if (!nextReset.isAfter(localNow)) {
            nextReset = nextReset.plusDays(1);
        }
        return nextReset.plusDays(Math.max(0, cycleCount - 1L)).toInstant();
    }
}
