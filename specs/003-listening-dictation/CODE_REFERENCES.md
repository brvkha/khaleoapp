# Essential Code References & API Contracts

## 🔑 Key Data Structures

### Backend Entities
```java
// Located in: backend/src/main/java/com/khaleo/flashcard/entity/

Topic                      // id, name, slug (globally unique), status, timestamps
Exercise                   // id, topic_id, name, slug (unique per-topic), orderIndex, status
Lesson                     // id, exercise_id, name, slug (unique per-exercise), orderIndex, mediaUrl, mediaType, status
Sentence                   // id, lesson_id, orderIndex, transcript, translation, aliasesJson, mediaUrl, startTime, endTime
UserSentenceProgress       // id, user_id, sentence_id, isCompleted, completionSource (correct_check|skip), completedAt
```

### Frontend Types
```typescript
// Located in: frontend/src/features/listening/services/listeningApi.ts

interface SentenceWithProgress {
  id: UUID;
  orderIndex: number;
  transcript: string;
  translation: string | null;
  aliasesJson: string | null;
  mediaUrl: string | null;
  startTime: number | null;
  endTime: number | null;
  isCompleted: boolean;
}

interface LessonWorkspace {
  lessonId: UUID;
  name: string;
  slug: string;
  mediaUrl: string | null;
  mediaType: 'audio' | 'video' | null;
  sentences: SentenceWithProgress[];
  progressPercent: number;
  completedCount: number;
  totalCount: number;
}

interface ProgressUpdateResponse {
  progressId: UUID;
  sentenceId: UUID;
  completed: boolean;
  completionSource: 'correct_check' | 'skip';
  lessonProgressPercent: number;
  lessonCompletedCount: number;
  lessonTotalCount: number;
}
```

---

## 📡 API Endpoints Summary

### Admin CMS (Phase 3 - Completed)
```
Topic Management
  POST   /api/v1/admin/listening/topics
  GET    /api/v1/admin/listening/topics
  GET    /api/v1/admin/listening/topics/{topicId}
  PUT    /api/v1/admin/listening/topics/{topicId}
  DELETE /api/v1/admin/listening/topics/{topicId}

Exercise Management
  POST   /api/v1/admin/listening/topics/{topicId}/exercises
  GET    /api/v1/admin/listening/topics/{topicId}/exercises
  PUT    /api/v1/admin/listening/exercises/{exerciseId}
  DELETE /api/v1/admin/listening/exercises/{exerciseId}

Lesson Management
  POST   /api/v1/admin/listening/exercises/{exerciseId}/lessons
  GET    /api/v1/admin/listening/exercises/{exerciseId}/lessons
  PUT    /api/v1/admin/listening/lessons/{lessonId}
  DELETE /api/v1/admin/listening/lessons/{lessonId}

Sentence Management
  POST   /api/v1/admin/listening/lessons/{lessonId}/sentences
  GET    /api/v1/admin/listening/lessons/{lessonId}/sentences
  PUT    /api/v1/admin/listening/sentences/{sentenceId}
  DELETE /api/v1/admin/listening/sentences/{sentenceId}

Sentence Operations
  PUT    /api/v1/admin/listening/lessons/{lessonId}/sentences/reorder
  POST   /api/v1/admin/listening/lessons/{lessonId}/sentences/import-json
```

### Learner APIs (Phase 4 - Partially completed)
```
Catalogue Discovery (T038c - Pending)
  GET /api/v1/listening/topics
  GET /api/v1/listening/topics/{topicSlug}/exercises/{exerciseSlug}
  GET /api/v1/listening/lessons/{lessonSlug}

Workspace & Progress (T041 - Pending)
  GET  /api/v1/listening/lessons/{lessonId}/workspace
  POST /api/v1/listening/progress/sentences/{sentenceId}
  GET  /api/v1/listening/progress/sentences/{sentenceId}

Media & Dictionary (Partial - T055, T056)
  POST /api/v1/listening/media/access
  GET  /api/v1/listening/dictionary?term={word}
```

---

## 🔧 Service Layer Architecture

### Backend Services

```java
// Admin Services (Phase 3 - Complete)
AdminListeningCrudService          // Topic/Exercise/Lesson/Sentence CRUD
AdminSentenceReorderService        // Sentence drag-drop reorder
AdminSentenceImportService         // JSON bulk import with validation

// Learner Services (Phase 4 - Partial)
LearnerCatalogueService            // ✅ Topics/Exercises/Lessons retrieval
LearnerListeningWorkspaceService   // ✅ Lesson with progress
LearnerSentenceProgressService     // ✅ Progress upsert & completion

// Shared Services
DictationNormalizer                // ✅ Unicode + punctuation stripping
SentenceMediaValidationService     // ✅ Timestamp validation
ListeningMediaAccessService        // ⏳ S3 presigned URLs
DictionaryProxyService             // ⏳ Cambridge API proxy
```

### Frontend Services & Hooks

```typescript
// API Client
listeningApi.ts                    // ✅ All learner endpoints typed

// Hooks
useDictationSession                // ✅ State management with normalization
useListeningAudioPlayer            // ✅ Blob playback

// Stores
listeningSettingsStore             // Zustand + localStorage
```

---

## 🧠 Core Algorithms

### 1. Dictation Normalization
```typescript
// Located in: frontend/src/features/listening/hooks/useDictationSession.ts

function normalizeDictation(text: string): string {
  return text
    .normalize('NFKC')                    // Unicode normalization
    .toLowerCase()                        // Lowercase
    .replace(/[\p{P}\p{S}]/gu, '')       // Strip punctuation & symbols
    .replace(/\s+/g, ' ')                // Collapse whitespace
    .trim();                             // Trim edges
}

// Example:
// "Hello, world!" → "hello world"
// "Café" → "cafe"
// "What's up?" → "whats up"
```

### 2. Left-to-Right Masking for UI
```typescript
// Located in: frontend/src/features/listening/hooks/useDictationSession.ts

function generateMaskDisplay(userInput: string, target: string, isCorrect: boolean): string {
  if (isCorrect) return userInput;

  const userTokens = userInput.split(/\s+/);
  const targetTokens = target.split(/\s+/);
  const result: string[] = [];

  for (let i = 0; i < targetTokens.length; i++) {
    if (i < userTokens.length) {
      const userNorm = userTokens[i].toLowerCase().replace(/[\p{P}\p{S}]/gu, '');
      const targetNorm = targetTokens[i].toLowerCase().replace(/[\p{P}\p{S}]/gu, '');
      
      if (userNorm === targetNorm) {
        result.push(userTokens[i]);      // Show correct token as-is
      } else {
        result.push('***');               // Mask & stop
        break;
      }
    } else {
      result.push('***');
      break;
    }
  }
  return result.join(' ');
}

// Example:
// User typed: "The cat sat"
// Target: "The cat sat on the mat"
// Result: "The cat sat ***"
//
// User typed: "The dog sat"
// Target: "The cat sat on the mat"
// Result: "The *** *** *** *** *** ***"
```

### 3. Progress Calculation
```java
// Located in: backend/src/main/java/com/khaleo/flashcard/service/listening/LearnerSentenceProgressService.java

int progressPercent = sentenceCount == 0 ? 0 : 
    (int) ((completedCount * 100) / sentenceCount);

// Only incremented when:
// - actionType == "correct_check" AND answer matches exactly, OR
// - actionType == "skip"

// Never incremented on:
// - Incorrect checks (actionType != "correct_check" || answer != target)
```

---

## 🎯 Repository Methods

### Backend Repositories Required

```java
// TopicRepository
Optional<Topic> findBySlug(String slug);
List<Topic> findAllByStatus(String status);

// ExerciseRepository
Optional<Exercise> findByTopicIdAndSlug(UUID topicId, String slug);
List<Exercise> findAllByTopicIdAndStatus(UUID topicId, String status);

// LessonRepository
Optional<Lesson> findByExerciseIdAndSlug(UUID exerciseId, String slug);
List<Lesson> findAllByExerciseIdAndStatus(UUID exerciseId, String status);
List<Lesson> findAllByIdOrderByOrderIndexAsc(UUID lessonId);

// SentenceRepository
List<Sentence> findAllByLessonIdOrderByOrderIndexAsc(UUID lessonId);
long countByLessonId(UUID lessonId);

// UserSentenceProgressRepository
Optional<UserSentenceProgress> findByUserIdAndSentenceId(UUID userId, UUID sentenceId);
List<UserSentenceProgress> findAllByUserIdAndSentenceIds(UUID userId, List<UUID> sentenceIds);
long findCompletedCountByLessonIdAndUserId(UUID lessonId, UUID userId);
```

---

## 🧪 Test Patterns

### Backend Contract Test Pattern
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminListeningContractTest {
  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @Autowired TopicRepository topicRepository;

  @Test
  void createTopicReturns201() throws Exception {
    ListeningDtos.TopicDto request = new ListeningDtos.TopicDto(
        null, "Name", "slug", "desc", "draft", null, null);
    
    mockMvc.perform(post("/api/v1/admin/listening/topics")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id", notNullValue()));
  }
}
```

### Frontend Hook Test Pattern
```typescript
import { renderHook, act } from '@testing-library/react';
import { useDictationSession } from './useDictationSession';

describe('useDictationSession', () => {
  it('should mark sentence complete on correct check', async () => {
    const sentences = [{ id: '1', transcript: 'test', isCompleted: false }];
    const { result } = renderHook(() => useDictationSession('lesson-1', sentences));

    act(() => {
      result.current.setCurrentAnswer('test');
      result.current.checkAnswer();
    });

    expect(result.current.checkResult?.correct).toBe(true);
    expect(result.current.completedCount).toBe(1);
  });
});
```

---

## 🔐 Security Guards

### Progress Completion Guard
```java
public ProgressUpdateResponse markSentenceComplete(UUID sentenceId, String actionType) {
  // ✅ GUARD: Only allow valid actions
  if (!isValidAction(actionType)) {
    throw new IllegalArgumentException("Invalid action type");
  }

  // ✅ GUARD: Only proceed if correct_check or skip
  private boolean isValidAction(String action) {
    return "correct_check".equals(action) || "skip".equals(action);
  }

  // ✅ GUARD: Incorrect checks never reach this code
  // (They're filtered at API layer in caller)
}
```

### User-Scoped Progress
```java
private UUID getCurrentUserId() {
  Authentication auth = SecurityContextHolder.getContext().getAuthentication();
  if (auth == null || !auth.isAuthenticated()) {
    throw new IllegalStateException("User not authenticated");
  }
  // Extract from auth.getPrincipal()
  return getUserIdFromPrincipal(auth.getPrincipal());
}
```

---

## 📚 Configuration & Constants

### Frontend Config
```typescript
// Located in: frontend/src/features/listening/config/listeningConfig.ts

export const LISTENING_CONFIG = {
  SPEED_OPTIONS: [0.25, 0.5, 0.75, 1, 1.25, 1.5, 1.75, 2, 2.5, 3],
  DEFAULT_SPEED: 1,
  DEFAULT_REPLAY_KEY: 'ctrl', // or 'cmd' on Mac
  DEFAULT_PLAY_KEY: '`',
  AUTO_PLAY_ON_NEXT: true,
  AUTO_REPLAY_COUNT: 0,
  REPLAY_INTERVAL_MS: 500,
};
```

### Backend Config
```yaml
# Located in: backend/src/main/resources/application.yml

listening:
  media:
    presign-ttl-seconds: 3600
  dictionary:
    cambridge-api-key: ${CAMBRIDGE_API_KEY:placeholder}
    cache-ttl-hours: 24
```

---

## 🚨 Common Pitfalls

1. **Forgetting getCurrentUserId() implementation**
   - Currently returns placeholder UUID
   - Need to wire to auth system

2. **UserSentenceProgressRepository custom queries**
   - Spring Data doesn't auto-generate complex queries
   - Must implement custom methods or use @Query

3. **Timestamp timezone issues**
   - Use Instant.now() (UTC), not LocalDateTime
   - All timestamps should be in UTC

4. **Normalization must be bidirectional**
   - Normalize both user input AND target transcript
   - Otherwise: "Café" won't match "cafe"

5. **UI Masking must use ORIGINAL text for display**
   - Mask calculation uses normalized text
   - Display output uses original tokens
   - Otherwise: punctuation gets lost in feedback

---

## ✅ Pre-Deploy Checklist

- [ ] All services have proper error handling
- [ ] Repository queries tested with sample data
- [ ] Normalization function handles edge cases (emoji, special chars)
- [ ] UI masking doesn't break on empty input
- [ ] Progress only increments on valid actions
- [ ] User ID extraction wired to auth system
- [ ] API contracts match OpenAPI spec
- [ ] No hardcoded UUIDs in tests
- [ ] All services transactional or read-only
- [ ] Logging in place for debugging

---

**Last Updated**: April 21, 2026  
**Maintainer**: [Next Developer]  
**Questions?**: See IMPLEMENTATION_SUMMARY.md or PHASE_4_6_ROADMAP.md
