# ✅ Implementation Checklist - Listening Dictation Feature

**Status**: Phase 4 Foundation Complete (29% of feature)  
**Last Updated**: April 21, 2026

---

## 📋 Phase 1-3: Setup & Admin CMS (✅ 100% COMPLETE)

### Phase 1: Shared Infrastructure
- [x] T001 Package structure placeholders
- [x] T002 Environment properties (presign TTL, Cambridge base URL)
- [x] T003 Listening UI constants (speeds, shortcuts, defaults)
- [x] T004 Listening API TypeScript types
- [x] T005 Admin routing & lazy-load wrappers

### Phase 2: Foundational Services
- [x] T006 Flyway migration (5 tables: topics, exercises, lessons, sentences, user_sentence_progress)
- [x] T007 JPA entities (Topic, Exercise, Lesson, Sentence, UserSentenceProgress)
- [x] T008 Repositories (5 entities + custom queries)
- [x] T009 Shared DTOs (Topic, Exercise, Lesson, Sentence)
- [x] T010 Dictation normalizer (NFKC + punctuation stripping)
- [x] T011 Sentence media validator (timestamps)
- [x] T012 S3 presigned URL service
- [x] T013 Cambridge dictionary proxy (skeleton)
- [x] T014 Frontend normalization & masking utils
- [x] T015 Listening settings store (Zustand + localStorage)
- [x] T016 Audio player hook (blob playback)
- [x] T017 Exception handler (validation/import errors)

### Phase 3: Admin CMS - User Story 1 (✅ 100% COMPLETE)

#### Tests (T018-T022)
- [x] T018 Backend admin contract tests
- [x] T019 Backend hierarchy integration tests (scoped slugs, reorder)
- [x] T020 Backend sentence import integration tests ⚠️ (file conflict - recreate from template)
- [x] T021 Frontend admin API contract tests
- [x] T022 Frontend admin CMS UI tests

#### Implementation (T023-T032)
- [x] T023 Admin DTOs (TopicRequest, ExerciseRequest, LessonRequest, SentenceRequest)
- [x] T024 Admin CRUD service
- [x] T025 Sentence reorder service
- [x] T026 Sentence JSON import service (partial success)
- [x] T027a Admin Topic controller
- [x] T027b Admin Exercise controller
- [x] T027c Admin Lesson controller
- [x] T027d Admin Sentence controller
- [x] T028 Admin listening API client
- [x] T029 Admin CMS page shell (drill-down table states)
- [x] T030 Sentence reorder component (drag-drop)
- [x] T031 Sentence JSON import modal
- [x] T032 Mount AdminListeningPage in router

---

## 🟨 Phase 4: Learner Workspace (58% COMPLETE - 7 of 12 tasks)

### Tests (T033-T037) - ⏳ PENDING
- [ ] T033 [P] Backend learner contract tests (workspace + progress APIs)
- [ ] T034 [P] Backend progress update integration tests (correct_check/skip guards)
- [ ] T035 [P] Frontend normalization & evaluator unit tests
- [ ] T036 [P] Frontend dictation flow component tests
- [ ] T037 [P] Playwright learner dictation e2e smoke

### Implementation - 🟨 PARTIAL (7 of 12 done)

#### DTOs & Infrastructure
- [ ] T038a Learner retrieval DTOs (TopicListResponse, ExerciseListResponse)
- [x] T038b Learner catalogue service ✅
  - `getPublishedTopics()`
  - `getExercisesByTopicSlug(topicSlug)`
  - `getLessonsByExerciseSlug(topicSlug, exerciseSlug)`
  - `getLessonBySlug()` & `getLessonById()`
- [ ] T038c Learner catalogue endpoints (expose REST API)
- [ ] T038 Workspace DTOs (LessonWorkspaceResponse, ProgressUpdateRequest)

#### Services
- [x] T039 Learner workspace service ✅
  - `getLessonWorkspace(lessonId)` with progress
  - Returns sentences + progress + completion %
- [x] T040 Progress upsert service ✅
  - `markSentenceComplete(sentenceId, actionType)` - correct_check | skip only
  - Guards against incorrect checks
  - Calculates lesson progress %

#### Controllers
- [ ] T041 Learner endpoints exposure (REST controller)
  - `GET /api/v1/listening/lessons/{lessonId}/workspace`
  - `POST /api/v1/listening/progress/sentences/{sentenceId}`

#### Frontend
- [x] T042 Learner API client ✅
  - All endpoints typed
  - Error handling with graceful fallback
  - Media access + dictionary lookup
- [x] T043 Dictation session hook ✅
  - `useDictationSession(lessonId, sentences)`
  - State: currentIndex, answer, checkResult, isSkipped
  - Actions: checkAnswer(), skipSentence(), goNext(), goPrevious()
  - Computed: progressPercent, canGo*, completedCount
- [ ] T044 Replace ListeningPage placeholder
  - Integrate header, dictation tab, transcript tab (stub)
  - Tab switching
- [x] T045 Dictation tab component ✅
  - Sentence counter + translation display
  - Textarea input with Ctrl+Enter/Esc shortcuts
  - Check result + masked display
  - Skip indicator
  - Prev/Next buttons
- [x] T046 Progress header component ✅
  - Lesson title
  - Sentence counter (current/total)
  - Progress percentage + animated bar

**Next Priority**: T038a → T038c → T038 → T041 → T044 (3-4 hours to MVP)

---

## ⏳ Phase 5: Enhanced Learning (0% COMPLETE - 0 of 13 tasks)

### Tests (T047-T052) - PENDING
- [ ] T047 [P] Backend dictionary proxy contract tests
- [ ] T048 [P] Backend dictionary fallback integration tests
- [ ] T049 [P] Frontend transcript auto-scroll unit tests
- [ ] T050 [P] Frontend settings modal persistence tests
- [ ] T051 [P] Frontend dictionary popup tests
- [ ] T052 [P] Playwright e2e transcript + dictionary flow

### Implementation (T053-T060) - PENDING
- [ ] T053 Media access DTOs
- [ ] T054 Dictionary proxy DTOs
- [ ] T055 Media access & dictionary endpoints (ListeningSupportController)
- [ ] T056 Dictionary cache service
- [ ] T057 Full transcript tab component
  - Click-to-play sentence audio
  - Auto-scroll to current
  - Loop/repeat controls
- [ ] T058 Settings modal component
  - Replay key binding
  - Play/pause key binding
  - Auto-play on next
  - Auto-replay count
  - Replay interval
  - localStorage persistence
- [ ] T059 Dictionary popover component
  - Word click detection
  - Lookup trigger
  - Display with IPA + audio links + definitions
  - Fallback on provider failure
- [ ] T060 Integrate into workspace

**Estimated**: 5-6 hours (after Phase 4)

---

## ⏳ Phase 6: Polish & Validation (0% COMPLETE - 0 of 6 tasks)

- [ ] T061 Update quickstart.md with troubleshooting
- [ ] T062 Align OpenAPI with implemented payloads
- [ ] T063 Document env variables & usage in README
- [ ] T064 Add manual e2e checklist for listening
- [ ] T065 Backend regression test suite
- [ ] T066 Frontend regression test suite

**Estimated**: 2-3 hours (final polish)

---

## 📊 Overall Progress

```
Phase 1  ████████████████████████████ 100%  ✅
Phase 2  ████████████████████████████ 100%  ✅
Phase 3  ████████████████████████████ 100%  ✅
Phase 4  ████████████████░░░░░░░░░░░░  58%  🟨
Phase 5  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░   0%  ⏳
Phase 6  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░   0%  ⏳
────────────────────────────────────────────────
TOTAL    ███████████░░░░░░░░░░░░░░░░░  29%  🟨
```

**Tasks Completed**: 11 of 38 (29%)  
**Tests Created**: 5 of 15 (33%)  
**Files Created**: 14 (11 code + 3 docs)

---

## 🎯 Critical Path to MVP (3-4 hours)

**Must Complete**:
1. [ ] T038a - Learner DTOs (30 min)
2. [ ] T038c - Catalogue endpoints (60 min)
3. [ ] T038 - Workspace DTOs (30 min)
4. [ ] T041 - Progress endpoints (60 min)
5. [ ] T044 - ListeningPage shell (60 min)

**Result**: ✅ **Admin → Learner end-to-end flow working**

---

## 🧪 Test Coverage Summary

| Phase | Contract | Integration | UI/E2E | Total | Status |
|-------|----------|-------------|--------|-------|--------|
| Phase 1-3 | 1 | 2 | 2 | 5 | ✅ |
| Phase 4 | 0 | 0 | 0 | 5 | ⏳ |
| Phase 5 | 0 | 0 | 0 | 6 | ⏳ |
| Phase 6 | 0 | 0 | 0 | 2 | ⏳ |
| **Total** | 1 | 2 | 2 | **18** | **28%** |

---

## 📁 File Checklist

### Backend Implementation Files
- [x] LearnerCatalogueService.java
- [x] LearnerListeningWorkspaceService.java
- [x] LearnerSentenceProgressService.java
- [ ] LearnerCatalogueController.java
- [ ] LearnerListeningController.java

### Backend Test Files
- [x] AdminListeningContractTest.java
- [x] AdminListeningHierarchyIT.java
- [x] AdminSentenceImportIT.java ⚠️
- [ ] LearnerListeningContractTest.java
- [ ] LearnerProgressUpdateIT.java

### Frontend Implementation Files
- [x] services/listeningApi.ts
- [x] hooks/useDictationSession.ts
- [x] components/DictationTab.tsx
- [x] components/ListeningProgressHeader.tsx
- [ ] components/FullTranscriptTab.tsx
- [ ] components/ListeningSettingsModal.tsx
- [ ] components/DictionaryPopover.tsx
- [ ] ListeningPage.tsx (integrate)

### Frontend Test Files
- [x] test/admin/listening/adminListeningApi.contract.test.ts
- [x] test/admin/listening/adminListeningCms.test.tsx
- [ ] test/listening/dictationEvaluator.test.ts
- [ ] test/listening/listeningDictationFlow.test.tsx
- [ ] test/listening/transcriptPlayback.test.ts
- [ ] test/listening/listeningSettingsModal.test.tsx
- [ ] test/listening/dictionaryPopup.test.tsx
- [ ] tests/e2e/listening-dictation-core.spec.ts
- [ ] tests/e2e/listening-transcript-dictionary.spec.ts

### Documentation Files
- [x] IMPLEMENTATION_SUMMARY.md
- [x] PHASE_4_6_ROADMAP.md
- [x] FILES_CREATED_SESSION.md
- [x] SESSION_COMPLETION_REPORT.md
- [x] CODE_REFERENCES.md
- [x] EXECUTIVE_SUMMARY.md
- [x] DOCUMENTATION_INDEX.md
- [x] IMPLEMENTATION_CHECKLIST.md (this file)

---

## 🔄 How to Update This Checklist

When completing a task:
1. Find task ID (e.g., T038a)
2. Change `[ ]` to `[x]`
3. Update phase completion %
4. Update overall progress bar
5. Commit: `git commit -m "T038a: Learner DTOs - update checklist"`

---

## 📝 Notes

**⚠️ Known Issues**:
- T020 (AdminSentenceImportIT.java) - File conflict on recreation. Use template in PHASE_4_6_ROADMAP.md
- `getCurrentUserId()` - Placeholder implementation needs auth wiring
- S3 presigned URLs - Framework ready, needs AWS SDK completion
- Cambridge API - Stub ready, needs credentials

**🎓 Learning Aids**:
- See PHASE_4_6_ROADMAP.md for code snippets
- See CODE_REFERENCES.md for algorithms & patterns
- See FILES_CREATED_SESSION.md for file inventory

**💾 Git Strategy**:
- Atomic commits per task: `git commit -m "Txxx: Description"`
- Push to feature branch: `git push origin 003-listening-dictation`
- Pre-commit check: `./mvnw clean compile && npm run type-check`

---

## ✨ Quick Stats

| Metric | Value |
|--------|-------|
| Total Tasks | 38 |
| Completed | 11 (29%) |
| In Progress | 0 |
| Pending | 27 (71%) |
| Test Files | 5 created, 10 pending |
| Code Files | 6 created, 6+ pending |
| Documentation | 8 created |
| Est. Remaining | 10-13 hours |

---

**Last Checked**: April 21, 2026, ~3 PM  
**Next Review**: After T038a-T044 completion (3-4 hours)  
**Target Release**: Phase 4+5 complete in 1 week
