# 07 - Backend Design (Spring Boot + FSRS v6)

## Package Structure

```
com/khaleo/flashcard/
├─ controller/deck/
│  ├─ PrivateWorkspaceController.java
│  └─ DeckController.java
├─ controller/card/CardController.java
├─ controller/study/StudySessionController.java
├─ service/deck/
│  ├─ PrivateDeckCrudService.java
│  └─ DeckStatsService.java
├─ service/study/
│  ├─ SpacedRepetitionService.java ⭐ FSRS v6 logic
│  ├─ StudyRatingService.java
│  ├─ NextCardsService.java
│  └─ StudySchedulerService.java
├─ entity/
│  ├─ CardLearningState.java
│  ├─ Card.java
│  └─ Deck.java
└─ repository/
   ├─ CardLearningStateRepository.java
   └─ CardRepository.java
```

## CardLearningState Entity

```java
@Entity
@Table(name = "card_learning_states")
public class CardLearningState {
  @Id UUID id;
  @ManyToOne UUID user_id;
  @ManyToOne UUID card_id;
  
  // FSRS state
  @Enumerated CardLearningStateType state;  // NEW, LEARNING, REVIEW, RELEARNING
  BigDecimal fsrs_stability;      // Khả năng nhớ
  BigDecimal fsrs_difficulty;     // Độ khó thẻ
  Integer fsrs_reps;              // Số lần ôn
  Integer fsrs_lapses;            // Số lần quên
  Integer learning_step_good_count;
  
  // Schedule
  Instant last_reviewed_at;
  Instant next_review_at;
  
  @Version Long version;  // Optimistic locking
}
```

## FSRS v6 Parameters

```
w0=1.2682  (init_stability_again)
w1=1.2682  (init_stability_hard)
w2=0.7310  (init_stability_good)
w3=1.7540  (init_stability_easy)
w4=7.9650  (init_difficulty_base)
w5=0.6470  (init_difficulty_delta)
w6=2.5935  (difficulty_update_slope)
w7=0.0010  (reserved)
w8=1.2670  (recall_growth_base)
w9=0.1510  (recall_stability_decay)
w10=1.5040 (recall_retrievability_gain)
w11=2.0287 (forget_base)
w12=0.0767 (forget_difficulty_exponent)
w13=0.4215 (forget_stability_exponent)
w14=2.5117 (forget_retrievability_gain)
w15=0.2713 (hard_penalty)
w16=1.3240 (easy_bonus)
w17=0.4372 (same_day_exponent)
w18=0.0468 (same_day_offset)

desired_retention = 0.9
```

## NextCardsService

```java
public NextCardsPageResponse getNextCards(UUID deckId, NextCardsRequest request) {
  UUID userId = requireAuthenticatedUserId(...);
  
  // Thẻ learning/relearning đến hạn
  List<CardLearningState> dueLearning = 
    repo.findByUserIdAndDeckIdAndStateInAndNextReviewDateLessThanEqual(
      userId, deckId, [LEARNING, RELEARNING], now);
  
  // Thẻ review/mastered đến hạn
  List<CardLearningState> dueReview = 
    repo.findByUserIdAndDeckIdAndStateInAndNextReviewDateLessThanEqual(
      userId, deckId, [REVIEW, MASTERED], now);
  
  // Thẻ mới (theo quota hôm nay)
  int newQuota = studySessionLimitService.remainingNewCardQuota(userId);
  List<Card> newCards = cardRepo.findUnseenCardsInDeck(deckId, userId, newQuota);
  
  // Sắp xếp: learning → review → new
  List<StudyCardSummary> ordered = dueLearning + dueReview + newCards;
  List<StudyCardSummary> pageItems = ordered.subList(offset, offset+size);
  
  return new NextCardsPageResponse(pageItems, nextToken, hasMore);
}
```

## StudyRatingService

```java
public RateCardResponse rateCard(UUID cardId, RateCardRequest request) {
  UUID userId = requireAuthenticatedUserId(...);
  Card card = cardRepo.findById(cardId);
  Instant now = Instant.now();
  
  // Get hoặc tạo learning state mới
  CardLearningState current = 
    cardLearningStateRepo.findByUserIdAndCardId(userId, cardId)
      .orElseGet(() => new CardLearningState(user, card));
  
  // Apply FSRS v6 logic
  RatingOutcome outcome = spacedRepetitionService.apply(current, request.rating(), now);
  
  // Update state
  current.setState(outcome.state());
  current.setNextReviewAt(outcome.nextReviewAt());
  current.setLastReviewedAt(now);
  current.setFsrsStability(outcome.stability());
  current.setFsrsDifficulty(outcome.difficulty());
  current.setFsrsReps(outcome.reps());
  current.setFsrsLapses(outcome.lapses());
  
  if (request.rating() == GOOD && state in [LEARNING, RELEARNING]) {
    current.setLearningStepGoodCount(current.getLearningStepGoodCount() + 1);
  } else {
    current.setLearningStepGoodCount(0);
  }
  
  // Persist with retry
  cardLearningStateRepo.saveAndFlush(current);
  
  // Log event
  studyActivityLogPublisher.publishRatingEvent(userId, cardId, ...);
  
  return new RateCardResponse(cardId, current.getState(), current.getNextReviewAt(), ...);
}
```

## API Endpoints

```
# Deck
GET /api/v1/private/decks?q={query}&page=0&size=50
POST /api/v1/private/decks { name, description }
PUT /api/v1/private/decks/{id} { name, description }
DELETE /api/v1/private/decks/{id}
GET /api/v1/private/decks/{id}/stats

# Card
GET /api/v1/private/decks/{id}/cards/search?frontText={q}&backText={q}&page={p}&size={sz}
POST /api/v1/decks/{id}/cards { term, answer }
PUT /api/v1/cards/{id} { term, answer, version }
DELETE /api/v1/cards/{id}

# Study Session
GET /api/v1/study-session/decks/{deckId}/next-cards?size=20
GET /api/v1/study-session/cards/{cardId}/preview-ratings
POST /api/v1/study-session/cards/{cardId}/rate { rating, timeSpentMs }
```

