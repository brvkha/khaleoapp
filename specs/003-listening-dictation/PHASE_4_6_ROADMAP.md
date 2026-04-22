# Phase 4-6 Implementation Roadmap

## Quick Status
- ✅ **Tests for US1**: 5/5 complete (T018-T022)
- 🟨 **Phase 4 Implementation**: 7/12 complete (58%)
- ⏳ **Phase 5 Implementation**: 0/7 complete (0%)
- ⏳ **Phase 6 Polish**: 0/6 complete (0%)

---

## 🎯 Critical Path to MVP

### Immediate (Next 2-3 hours)
To enable end-to-end admin→learner flow:

**T038a - Catalogue DTOs**
- Location: `backend/src/main/java/com/khaleo/flashcard/controller/listening/dto/`
- Create simple DTOs for topic/exercise lists (no progress info)

**T038c - Catalogue Controller** 
- Location: `backend/src/main/java/com/khaleo/flashcard/controller/listening/LearnerCatalogueController.java`
- Endpoints:
  - GET `/api/v1/listening/topics`
  - GET `/api/v1/listening/topics/{topicSlug}/exercises/{exerciseSlug}`
  - GET `/api/v1/listening/lessons/{lessonSlug}`

**T038 - Workspace DTOs**
- Use existing `ListeningDtos` or create response wrappers
- Ensure `LessonWorkspaceResponse` includes sentence list + progress

**T041 - Learner Controller**
- Location: `backend/src/main/java/com/khaleo/flashcard/controller/listening/LearnerListeningController.java`
- Endpoints:
  - GET `/api/v1/listening/lessons/{lessonId}/workspace` → calls `LearnerListeningWorkspaceService`
  - POST `/api/v1/listening/progress/sentences/{sentenceId}` → calls `LearnerSentenceProgressService`

**T044 - ListeningPage Shell**
- Location: `frontend/src/features/listening/ListeningPage.tsx`
- Layout:
  - Header: `<ListeningProgressHeader>`
  - Tabs: "Dictation" + "Transcript" (placeholder for now)
  - Active tab: `<DictationTab>`

### Short-term (Phase 4 completion)
- T033-T037: Add 5 test files for Phase 4
- Verify end-to-end: admin create → learner study → progress save

### Medium-term (Phase 5)
- Build transcript tab with auto-scroll
- Dictionary integration with fallback
- Settings modal with localStorage

### Final (Phase 6)
- Polish, docs, regression testing

---

## 📚 API Contracts Reference

### Learner Catalogue (T038c)
```
GET /api/v1/listening/topics
Response: TopicDto[]

GET /api/v1/listening/topics/{topicSlug}/exercises/{exerciseSlug}
Response: LessonDto[]

GET /api/v1/listening/lessons/{lessonSlug}
Response: LessonDto
```

### Learner Workspace (T041)
```
GET /api/v1/listening/lessons/{lessonId}/workspace
Response: {
  lessonId: UUID,
  name: string,
  sentences: SentenceWithProgress[],
  progressPercent: int,
  completedCount: long,
  totalCount: long
}

POST /api/v1/listening/progress/sentences/{sentenceId}
Request: { actionType: "correct_check" | "skip" }
Response: {
  progressId: UUID,
  sentenceId: UUID,
  completed: boolean,
  completionSource: string,
  lessonProgressPercent: int,
  lessonCompletedCount: long,
  lessonTotalCount: long
}
```

---

## 🛠️ Code Snippets for Quick Completion

### T041 - LearnerListeningController stub
```java
@RestController
@RequestMapping("/api/v1/listening")
@RequiredArgsConstructor
public class LearnerListeningController {
  
  private final LearnerListeningWorkspaceService workspaceService;
  private final LearnerSentenceProgressService progressService;

  @GetMapping("/lessons/{lessonId}/workspace")
  public Object getLessonWorkspace(@PathVariable UUID lessonId) {
    return workspaceService.getLessonWorkspace(lessonId);
  }

  @PostMapping("/progress/sentences/{sentenceId}")
  public Object markSentenceComplete(
      @PathVariable UUID sentenceId,
      @RequestBody Map<String, String> payload) {
    return progressService.markSentenceComplete(
        sentenceId, 
        payload.get("actionType"));
  }
}
```

### T044 - ListeningPage shell
```tsx
export function ListeningPage() {
  const [lesson, setLesson] = useState<LessonWorkspace | null>(null);
  const [activeTab, setActiveTab] = useState<'dictation' | 'transcript'>('dictation');

  useEffect(() => {
    // Load from URL param or nav state
    const lessonId = '...'; // From route/state
    listeningApi.getLessonWorkspace(lessonId).then(setLesson);
  }, []);

  if (!lesson) return <div>Loading...</div>;

  return (
    <>
      <ListeningProgressHeader
        currentIndex={0}
        totalCount={lesson.totalCount}
        progressPercent={lesson.progressPercent}
        lessonName={lesson.name}
      />
      <div className="flex gap-4 p-4">
        <button onClick={() => setActiveTab('dictation')}>Dictation</button>
        <button onClick={() => setActiveTab('transcript')}>Transcript</button>
      </div>
      <div>
        {activeTab === 'dictation' && (
          <DictationTab
            lessonId={lesson.lessonId}
            sentences={lesson.sentences}
          />
        )}
        {activeTab === 'transcript' && (
          <div>Transcript tab (TODO: T057)</div>
        )}
      </div>
    </>
  );
}
```

---

## 🧪 Test File Templates

### T033 - Backend Learner Contract Test
Focus on:
- GET workspace returns correct DTOs
- POST progress with correct_check marks completed
- POST progress ignores incorrect checks (guard test!)
- Progress percentage calculated correctly

### T034 - Backend Integration Test  
Focus on:
- Creating sentences + progress records
- Verifying only correct_check and skip mark completion
- Incorrect checks don't update progress

### T035 - Frontend Normalization Test
Focus on:
- `normalizeDictation()` function with unicode/punctuation
- `generateMaskDisplay()` left-to-right token matching

### T036 - Frontend Component Test
Focus on:
- DictationTab renders sentences
- Check button calls API
- Skip button calls API
- Navigation prev/next works

### T037 - Playwright E2E
Focus on:
- Select topic → exercise → lesson
- Load dictation page
- Type answer + check
- Progress bar updates
- Navigate to next

---

## ⚠️ Known Issues / Considerations

1. **User Authentication**: `getCurrentUserId()` currently returns placeholder UUID
   - Will need to extract from `SecurityContextHolder.getContext().getAuthentication()`
   - Depends on auth system configuration

2. **UserSentenceProgressRepository**: Need custom query method
   ```java
   List<UserSentenceProgress> findAllByUserIdAndSentenceIds(UUID userId, List<UUID> sentenceIds);
   long findCompletedCountByLessonIdAndUserId(UUID lessonId, UUID userId);
   ```

3. **S3 Presigned URLs**: Not yet implemented in `ListeningMediaAccessService`
   - Will need AWS SDK integration

4. **Dictionary Proxy**: Stubbed in `DictionaryProxyService`
   - Needs Cambridge API credentials configuration

---

## 📊 Dependency Graph

```
T038c (Catalogue Endpoints)
  ↑ depends on T038b (LearnerCatalogueService) ✅

T041 (Learner Endpoints)
  ↑ depends on T039 (Workspace Service) ✅
  ↑ depends on T040 (Progress Service) ✅

T044 (ListeningPage)
  ↑ depends on T042 (listeningApi) ✅
  ↑ depends on T045 (DictationTab) ✅
  ↑ depends on T046 (ProgressHeader) ✅
  ↑ depends on T041 (Learner Endpoints)

T033-T037 (Tests)
  ↑ depends on T038c, T041, T044 being complete
```

---

## 💾 Save & Version Control

Before starting next implementation:
```bash
git add -A
git commit -m "Phase 4 foundation: learner services, API client, UI components (58% complete)"
git push origin 003-listening-dictation
```

Then for each subsequent task, create atomic commits:
```bash
git commit -m "T038a: Add catalogue DTOs"
git commit -m "T038c: Expose learner catalogue endpoints"
# etc.
```

---

## 🎓 Learning Resources

- **Dictation Normalization**: See `DictationNormalizer.java` + `normalizeDictation.ts`
- **Exact Match Logic**: `useDictationSession.ts` line ~80 (token comparison)
- **UI Masking Rule**: `generateMaskDisplay()` function for left-to-right feedback
- **Repository Queries**: Check `SentenceRepository` for order-preserving queries

---

**Total Estimated Remaining Work**: 
- Phase 4 completion: 3-4 hours
- Phase 5 features: 5-6 hours  
- Phase 6 polish: 2-3 hours
- **Total: 10-13 hours**

Current completion: **27% of full feature** (10 of 37 implementation tasks + 5 of 15 test tasks)
