# Tasks: Listening Dictation + Admin CMS (Phase 1)

**Input**: Design documents from `specs/003-listening-dictation/`
**Prerequisites**: `plan.md` (required), `spec.md` (required), `research.md`, `data-model.md`, `contracts/listening-dictation.openapi.yaml`, `quickstart.md`

**Tests**: Included because `spec.md` defines mandatory testing expectations and `quickstart.md` contains explicit backend/frontend/integration test checklists.

**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create listening feature scaffolding, config, and shared constants before domain implementation.

- [ ] T001 Create listening package structure placeholders in `backend/src/main/java/com/khaleo/flashcard/controller/admin/listening/.gitkeep`, `backend/src/main/java/com/khaleo/flashcard/controller/listening/.gitkeep` and `backend/src/main/java/com/khaleo/flashcard/service/listening/.gitkeep`
- [ ] T002 [P] Add listening environment properties (presign TTL, Cambridge base URL/key placeholders) in `backend/src/main/resources/application.yml`
- [ ] T003 [P] Add listening UI constants (speed options, shortcut defaults, replay options) in `frontend/src/features/listening/config/listeningConfig.ts`
- [ ] T004 [P] Add shared listening API TypeScript types in `frontend/src/features/listening/types/listeningApi.ts`
- [ ] T005 [P] Define admin listening route paths and lazy-load wrappers in `frontend/src/router/AppRouter.tsx`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Implement core schema, shared services, and cross-story primitives that all user stories depend on.

**CRITICAL**: Complete this phase before starting any user story.

- [ ] T006 Create Flyway migration for `topics`, `exercises`, `lessons`, `sentences`, and `user_sentence_progress` in `backend/src/main/resources/db/migration/V20260421_016__listening_dictation_schema.sql`
- [ ] T007 [P] Add JPA entities for listening domain in `backend/src/main/java/com/khaleo/flashcard/entity/{Topic.java,Exercise.java,Lesson.java,Sentence.java,UserSentenceProgress.java}`
- [ ] T008 [P] Add listening repositories in `backend/src/main/java/com/khaleo/flashcard/repository/{TopicRepository.java,ExerciseRepository.java,LessonRepository.java,SentenceRepository.java,UserSentenceProgressRepository.java}`
- [ ] T009 [P] Add shared listening DTO models in `backend/src/main/java/com/khaleo/flashcard/controller/listening/dto/{TopicDto.java,ExerciseDto.java,LessonDto.java,SentenceDto.java}`
- [ ] T010 Add strict dictation normalization utility in `backend/src/main/java/com/khaleo/flashcard/service/listening/DictationNormalizer.java`
- [ ] T011 [P] Add sentence media mode validator (`start_time/end_time` rules) in `backend/src/main/java/com/khaleo/flashcard/service/listening/SentenceMediaValidationService.java`
- [ ] T012 [P] Add S3 presigned URL issuer for lesson/sentence media in `backend/src/main/java/com/khaleo/flashcard/service/listening/ListeningMediaAccessService.java`
- [ ] T013 [P] Add Cambridge dictionary proxy client/service skeleton in `backend/src/main/java/com/khaleo/flashcard/service/listening/DictionaryProxyService.java`
- [ ] T014 [P] Add frontend normalization and masking utilities in `frontend/src/features/listening/utils/{normalizeDictation.ts,maskDictationResult.ts}`
- [ ] T015 [P] Add listening settings store with localStorage persistence in `frontend/src/store/listeningSettingsStore.ts`
- [ ] T016 [P] Add blob/object URL playback hook in `frontend/src/features/listening/hooks/useListeningAudioPlayer.ts`
- [ ] T017 Add listening exception mapping for validation/import errors in `backend/src/main/java/com/khaleo/flashcard/controller/listening/ListeningExceptionHandler.java`

**Checkpoint**: Foundation complete, user stories can proceed.

---

## Phase 3: User Story 1 - Manage Listening Content in Admin CMS (Priority: P1) 🎯 MVP

**Goal**: Deliver complete admin drill-down CMS (Topic -> Exercise -> Lesson -> Sentence) with reorder and JSON import.

**Independent Test**: Admin can create topic/exercise/lesson/sentence, reorder sentences, import JSON rows with partial success errors, and learner APIs return updated order.

### Tests for User Story 1

- [ ] T018 [P] [US1] Add admin contract tests for CRUD/reorder/import endpoints in `backend/src/test/java/com/khaleo/flashcard/contract/listening/AdminListeningContractTest.java`
- [ ] T019 [P] [US1] Add integration tests for scoped slug uniqueness and transactional reorder in `backend/src/test/java/com/khaleo/flashcard/integration/listening/AdminListeningHierarchyIT.java`
- [ ] T020 [P] [US1] Add integration tests for sentence JSON import partial success response in `backend/src/test/java/com/khaleo/flashcard/integration/listening/AdminSentenceImportIT.java`
- [ ] T021 [P] [US1] Add frontend admin API contract tests in `frontend/src/test/admin/listening/adminListeningApi.contract.test.ts`
- [ ] T022 [P] [US1] Add frontend admin CMS UI tests (drill-down + reorder + import) in `frontend/src/test/admin/listening/adminListeningCms.test.tsx`

### Implementation for User Story 1

- [ ] T023 [US1] Implement topic/exercise/lesson/sentence admin DTOs in `backend/src/main/java/com/khaleo/flashcard/controller/admin/listening/dto/{AdminTopicRequest.java,AdminExerciseRequest.java,AdminLessonRequest.java,AdminSentenceRequest.java}`
- [ ] T024 [US1] Implement admin hierarchy CRUD service in `backend/src/main/java/com/khaleo/flashcard/service/listening/AdminListeningCrudService.java`
- [ ] T025 [US1] Implement sentence reorder service in `backend/src/main/java/com/khaleo/flashcard/service/listening/AdminSentenceReorderService.java`
- [ ] T026 [US1] Implement sentence JSON import service with partial success in `backend/src/main/java/com/khaleo/flashcard/service/listening/AdminSentenceImportService.java`
- [ ] T027a [US1] Expose Admin Topic CRUD endpoints in `backend/src/main/java/com/khaleo/flashcard/controller/admin/listening/AdminTopicController.java`
- [ ] T027b [US1] Expose Admin Exercise CRUD endpoints in `backend/src/main/java/com/khaleo/flashcard/controller/admin/listening/AdminExerciseController.java`
- [ ] T027c [US1] Expose Admin Lesson CRUD endpoints in `backend/src/main/java/com/khaleo/flashcard/controller/admin/listening/AdminLessonController.java`
- [ ] T027d [US1] Expose Admin Sentence CRUD endpoints in `backend/src/main/java/com/khaleo/flashcard/controller/admin/listening/AdminSentenceController.java`
- [ ] T028 [US1] Add admin listening API client in `frontend/src/features/admin/listening/services/adminListeningApi.ts`
- [ ] T029 [US1] Implement admin listening CMS page shell with drill-down table states in `frontend/src/features/admin/listening/AdminListeningPage.tsx`
- [ ] T030 [US1] Implement sentence reorder drag-drop component in `frontend/src/features/admin/listening/components/SentenceReorderList.tsx`
- [ ] T031 [US1] Implement sentence JSON import modal and error table in `frontend/src/features/admin/listening/components/SentenceJsonImportModal.tsx`
- [ ] T032 [US1] Mount final AdminListeningPage component into the router and sidebar navigation in `frontend/src/components/Layout.tsx`

**Checkpoint**: US1 is independently functional and testable.

---

## Phase 4: User Story 2 - Complete Dictation Practice in Learner Workspace (Priority: P1)

**Goal**: Deliver strict dictation flow with check/skip, sentence navigation, keyboard shortcuts, and progress updates.

**Independent Test**: Learner can open lesson workspace, play sentence audio, submit correct/incorrect checks, skip with Esc, and see persisted progress percentage updates.

### Tests for User Story 2

- [ ] T033 [P] [US2] Add backend contract tests for learner workspace and progress update APIs in `backend/src/test/java/com/khaleo/flashcard/contract/listening/LearnerListeningContractTest.java`
- [ ] T034 [P] [US2] Add backend integration tests for `correct_check|skip` progress guard in `backend/src/test/java/com/khaleo/flashcard/integration/listening/LearnerProgressUpdateIT.java`
- [ ] T035 [P] [US2] Add frontend unit tests for normalization and exact-match evaluator in `frontend/src/test/listening/dictationEvaluator.test.ts`
- [ ] T036 [P] [US2] Add frontend component tests for dictation check/skip/shortcuts in `frontend/src/test/listening/listeningDictationFlow.test.tsx`
- [ ] T037 [P] [US2] Add Playwright learner dictation e2e smoke in `frontend/tests/e2e/listening-dictation-core.spec.ts`

### Implementation for User Story 2

- [ ] T038a [US2] Implement learner retrieval DTOs for catalogue in `backend/src/main/java/com/khaleo/flashcard/controller/listening/dto/{TopicListResponse.java,ExerciseListResponse.java}`
- [ ] T038b [US2] Implement learner catalogue query service (topics, exercises by topic, lesson by slug) in `backend/src/main/java/com/khaleo/flashcard/service/listening/LearnerCatalogueService.java`
- [ ] T038c [US2] Expose learner catalogue endpoints in `backend/src/main/java/com/khaleo/flashcard/controller/listening/LearnerCatalogueController.java`
- [ ] T038 [US2] Implement learner workspace response DTOs in `backend/src/main/java/com/khaleo/flashcard/controller/listening/dto/{LessonWorkspaceResponse.java,ProgressUpdateRequest.java}`
- [ ] T039 [US2] Implement learner workspace query service in `backend/src/main/java/com/khaleo/flashcard/service/listening/LearnerListeningWorkspaceService.java`
- [ ] T040 [US2] Implement progress upsert service and percent calculation in `backend/src/main/java/com/khaleo/flashcard/service/listening/LearnerSentenceProgressService.java`
- [ ] T041 [US2] Expose learner workspace/progress endpoints in `backend/src/main/java/com/khaleo/flashcard/controller/listening/LearnerListeningController.java`
- [ ] T042 [US2] Implement learner listening API client in `frontend/src/features/listening/services/listeningApi.ts`
- [ ] T043 [US2] Implement dictation state hook (answer, check result, skip, current index) in `frontend/src/features/listening/hooks/useDictationSession.ts`
- [ ] T044 [US2] Replace placeholder listening page with core dictation workspace in `frontend/src/features/listening/ListeningPage.tsx`
- [ ] T045 [US2] Implement dictation input panel with check/skip and prev/next controls in `frontend/src/features/listening/components/DictationTab.tsx`
- [ ] T046 [US2] Implement progress header and sentence counter in `frontend/src/features/listening/components/ListeningProgressHeader.tsx`

**Checkpoint**: US2 is independently functional and testable.

---

## Phase 5: User Story 3 - Use Transcript, Dictionary, and Settings for Better Learning (Priority: P2)

**Goal**: Deliver transcript playback controls, dictionary popup via backend proxy, and user settings for replay/auto-play behavior.

**Independent Test**: Learner can use full transcript click-to-play, auto-scroll, loop/repeat, open dictionary popup on word click, and continue studying when dictionary provider fails.

### Tests for User Story 3

- [ ] T047 [P] [US3] Add backend contract tests for media access and dictionary proxy endpoints in `backend/src/test/java/com/khaleo/flashcard/contract/listening/ListeningDictionaryMediaContractTest.java`
- [ ] T048 [P] [US3] Add backend integration tests for dictionary failure fallback payload in `backend/src/test/java/com/khaleo/flashcard/integration/listening/DictionaryProxyFallbackIT.java`
- [ ] T049 [P] [US3] Add frontend unit tests for transcript auto-scroll and loop logic in `frontend/src/test/listening/transcriptPlayback.test.ts`
- [ ] T050 [P] [US3] Add frontend component tests for settings modal persistence and shortcuts overrides in `frontend/src/test/listening/listeningSettingsModal.test.tsx`
- [ ] T051 [P] [US3] Add frontend component tests for dictionary popup success/fallback UX in `frontend/src/test/listening/dictionaryPopup.test.tsx`
- [ ] T052 [P] [US3] Add Playwright e2e for transcript + dictionary + settings flow in `frontend/tests/e2e/listening-transcript-dictionary.spec.ts`

### Implementation for User Story 3

- [ ] T053 [US3] Implement media access request/response DTOs in `backend/src/main/java/com/khaleo/flashcard/controller/listening/dto/{MediaAccessRequest.java,MediaAccessResponse.java}`
- [ ] T054 [US3] Implement dictionary proxy endpoint contract DTOs in `backend/src/main/java/com/khaleo/flashcard/controller/listening/dto/{DictionaryLookupResponse.java,DictionaryLookupFailureResponse.java}`
- [ ] T055 [US3] Expose media access and dictionary endpoints in `backend/src/main/java/com/khaleo/flashcard/controller/listening/ListeningSupportController.java`
- [ ] T056 [US3] Implement dictionary cache repository in `backend/src/main/java/com/khaleo/flashcard/repository/DictionaryCacheRepository.java` and service in `backend/src/main/java/com/khaleo/flashcard/service/listening/DictionaryCacheService.java`
- [ ] T057 [US3] Implement full transcript tab with click-to-play, auto-scroll, and loop controls in `frontend/src/features/listening/components/FullTranscriptTab.tsx`
- [ ] T058 [US3] Implement settings modal UI and localStorage sync in `frontend/src/features/listening/components/ListeningSettingsModal.tsx`
- [ ] T059 [US3] Implement clickable word dictionary popup component in `frontend/src/features/listening/components/DictionaryPopover.tsx`
- [ ] T060 [US3] Integrate transcript/settings/dictionary into listening workspace shell in `frontend/src/features/listening/ListeningPage.tsx`

**Checkpoint**: US3 is independently functional and testable.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Hardening, documentation, and release-readiness validation across all stories.

- [ ] T061 [P] Update feature quickstart troubleshooting and verification notes in `specs/003-listening-dictation/quickstart.md`
- [ ] T062 [P] Align OpenAPI details with implemented payloads/codes in `specs/003-listening-dictation/contracts/listening-dictation.openapi.yaml`
- [ ] T063 [P] Document listening learner/admin usage and env variables in `frontend/README.md` and `backend/README.md`
- [ ] T064 [P] Add manual local e2e checklist for listening workflows in `docs/manual-e2e/phase3-listening-local-checklist.md`
- [ ] T065 Run backend listening regression suite in `backend/src/test/java/com/khaleo/flashcard/integration/listening/ListeningFeatureRegressionIT.java`
- [ ] T066 [P] Run frontend unit and e2e listening suites and capture outcomes in `docs/manual-e2e/phase3-listening-local-checklist.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- Phase 1 -> no dependencies.
- Phase 2 -> depends on Phase 1 and blocks all user stories.
- Phase 3 (US1) -> depends on Phase 2.
- Phase 4 (US2) -> depends on Phase 2 and consumes data/contracts produced by US1 entities.
- Phase 5 (US3) -> depends on Phase 2 and extends learner flow delivered in US2.
- Phase 6 -> depends on completion of all targeted stories.

### User Story Dependency Graph

- `US1 (P1)` -> establishes authoring hierarchy and sentence ordering/import pipeline.
- `US2 (P1)` -> depends on foundational schema/services and requires published lesson/sentence data from US1.
- `US3 (P2)` -> depends on US2 learner workspace baseline and adds transcript/dictionary/settings enhancements.

### Within-Story Execution Rules

- Write tests for each story first and confirm they fail before implementation.
- Implement data/DTO models before service logic.
- Implement services before controller endpoints.
- Wire frontend API client before page/component integrations.
- Validate each story independently at its checkpoint.

### Parallel Opportunities

- Phase 1 tasks marked `[P]` can run in parallel.
- Phase 2 backend primitives (`T007-T013`) can run in parallel after migration baseline `T006` starts.
- US1 tests `T018-T022` can run in parallel.
- US2 tests `T033-T037` can run in parallel.
- US3 tests `T047-T052` can run in parallel.
- Frontend and backend implementation tracks within each story can run concurrently once shared contracts are stable.

---

## Parallel Example: User Story 1

```bash
# Parallel test authoring
T018 backend/src/test/java/com/khaleo/flashcard/contract/listening/AdminListeningContractTest.java
T020 backend/src/test/java/com/khaleo/flashcard/integration/listening/AdminSentenceImportIT.java
T022 frontend/src/test/admin/listening/adminListeningCms.test.tsx

# Parallel implementation slices
T025 backend/src/main/java/com/khaleo/flashcard/service/listening/AdminSentenceReorderService.java
T026 backend/src/main/java/com/khaleo/flashcard/service/listening/AdminSentenceImportService.java
T030 frontend/src/features/admin/listening/components/SentenceReorderList.tsx
```

## Parallel Example: User Story 2

```bash
# Parallel tests
T035 frontend/src/test/listening/dictationEvaluator.test.ts
T036 frontend/src/test/listening/listeningDictationFlow.test.tsx
T037 frontend/tests/e2e/listening-dictation-core.spec.ts

# Parallel implementation slices
T039 backend/src/main/java/com/khaleo/flashcard/service/listening/LearnerListeningWorkspaceService.java
T040 backend/src/main/java/com/khaleo/flashcard/service/listening/LearnerSentenceProgressService.java
T045 frontend/src/features/listening/components/DictationTab.tsx
```

## Parallel Example: User Story 3

```bash
# Parallel tests
T049 frontend/src/test/listening/transcriptPlayback.test.ts
T050 frontend/src/test/listening/listeningSettingsModal.test.tsx
T051 frontend/src/test/listening/dictionaryPopup.test.tsx

# Parallel implementation slices
T055 backend/src/main/java/com/khaleo/flashcard/controller/listening/ListeningSupportController.java
T057 frontend/src/features/listening/components/FullTranscriptTab.tsx
T059 frontend/src/features/listening/components/DictionaryPopover.tsx
```

---

## Implementation Strategy

### MVP First (User Story 1)

1. Complete Phase 1 setup.
2. Complete Phase 2 foundational layer.
3. Deliver Phase 3 (US1) end-to-end for admin content operations.
4. Validate US1 independently before starting learner stories.

### Incremental Delivery

1. Add US2 strict dictation learner flow after admin content foundation is ready.
2. Add US3 transcript/dictionary/settings enhancements on top of US2 baseline.
3. Finish with Phase 6 cross-cutting hardening, docs, and regression runs.

### Suggested MVP Scope

- MVP release scope: **Phase 1 + Phase 2 + Phase 3 (US1 only)**.
- Follow-up scope: **US2 learner dictation core**, then **US3 transcript/dictionary/settings**.

---

## Notes

- All tasks follow required checklist format: `- [ ] Txxx [P?] [US?] Description with file path`.
- Story labels are included only on user story tasks.
- `[P]` marks tasks designed to avoid same-file conflicts and allow parallel execution.
- Task IDs follow the format `Txxx`. Alpha-suffixes (e.g., `T027a`, `T027b`) are explicitly permitted in this project to denote sub-tasks belonging to a logical group without requiring a full renumbering of the sequence.
