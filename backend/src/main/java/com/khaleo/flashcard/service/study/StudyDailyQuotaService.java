package com.khaleo.flashcard.service.study;

import com.khaleo.flashcard.entity.User;
import com.khaleo.flashcard.entity.enums.CardLearningStateType;
import com.khaleo.flashcard.repository.CardLearningStateRepository;
import com.khaleo.flashcard.repository.UserRepository;
import com.khaleo.flashcard.service.persistence.PersistenceValidationExceptionMapper;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudyDailyQuotaService {

    private final UserRepository userRepository;
    private final CardLearningStateRepository cardLearningStateRepository;
    private final PersistenceValidationExceptionMapper exceptionMapper;

    @Value("${app.study.reset-hour:4}")
    private int resetHour;

    public int remainingNewCardQuota(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> exceptionMapper.missingRelationship("user", userId.toString()));

        ZoneId zone = resolveUserZone(user.getTimezone());
        ZonedDateTime localNow = Instant.now().atZone(zone);
        ZonedDateTime todayReset = localNow.toLocalDate().atTime(resetHour, 0).atZone(zone);
        ZonedDateTime currentWindowStart = localNow.isBefore(todayReset) ? todayReset.minusDays(1) : todayReset;

        Instant startOfDay = currentWindowStart.toInstant();
        Instant startOfNextDay = currentWindowStart.plusDays(1).toInstant();

        long studiedToday = cardLearningStateRepository
                .countByUserIdAndStateNotAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        userId,
                        CardLearningStateType.NEW,
                        startOfDay,
                        startOfNextDay);

        return Math.max(user.getDailyLearningLimit() - (int) studiedToday, 0);
    }

    private ZoneId resolveUserZone(String timezone) {
        try {
            return ZoneId.of(timezone == null || timezone.isBlank() ? "Asia/Ho_Chi_Minh" : timezone);
        } catch (RuntimeException ex) {
            return ZoneId.of("Asia/Ho_Chi_Minh");
        }
    }
}
