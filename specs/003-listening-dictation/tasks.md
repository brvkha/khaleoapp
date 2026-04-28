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

- [ ] T006 Create Flyway migration for `topics`, `exercises`, `lessons`, `sentences`, and `user_sentence_progress` with scoped slug constraints and `sentences -> user_sentence_progress` delete cascade (FR-037..FR-044) in `backend/src/main/resources/db/migration/V20260421_016__listening_dictation_schema.sql`
- [ ] T007 [P] Add JPA entities for listening domain in `backend/src/main/java/com/khaleo/flashcard/entity/{Topic.java,Exercise.java,Lesson.java,Sentence.java,UserSentenceProgress.java}`
- [ ] T008 [P] Add listening repositories in `backend/src/main/java/com/khaleo/flashcard/repository/{TopicRepository.java,ExerciseRepository.java,LessonRepository.java,SentenceRepository.java,UserSentenceProgressRepository.java}`
- [ ] T009 [P] Add shared listening DTO models in `backend/src/main/java/com/khaleo/flashcard/controller/listening/dto/{TopicDto.java,ExerciseDto.java,LessonDto.java,SentenceDto.java}`
- [ ] T010 Add strict dictation normalization utility in `backend/src/main/java/com/khaleo/flashcard/service/listening/DictationNormalizer.java`
- [ ] T011 [P] Add sentence media mode validator for `start_time >= 0` and `end_time > start_time` only (FR-016, FR-017) in `backend/src/main/java/com/khaleo/flashcard/service/listening/SentenceMediaValidationService.java`
- [ ] T012 [P] Add S3 presigned URL issuer for lesson/sentence media in `backend/src/main/java/com/khaleo/flashcard/service/listening/ListeningMediaAccessService.java`
- [ ] T013 [P] Add Cambridge dictionary proxy client/service skeleton in `backend/src/main/java/com/khaleo/flashcard/service/listening/DictionaryProxyService.java`
- [ ] T014 [P] Add frontend normalization and masking utilities in `frontend/src/features/listening/utils/{normalizeDictation.ts,maskDictationResult.ts}`
- [ ] T015 [P] Add listening settings store with localStorage persistence in `frontend/src/store/listeningSettingsStore.ts`
- [ ] T016 [P] Add blob/object URL playback hook in `frontend/src/features/listening/hooks/useListeningAudioPlayer.ts`
- [ ] T017 Add listening exception mapping for validation/import errors in `backend/src/main/java/com/khaleo/flashcard/controller/listening/ListeningExceptionHandler.java`
- [ ] T017a [P] Add correlation ID filter/interceptor (accept or generate request correlation ID, include response header, bind MDC context) in `backend/src/main/java/com/khaleo/flashcard/config/CorrelationIdFilter.java`
- [ ] T017b [P] Add structured listening flow logger helper for workspace/media/progress/dictionary events and failures (FR-049, FR-050) in `backend/src/main/java/com/khaleo/flashcard/service/listening/ListeningStructuredLogger.java`

**Checkpoint**: Foundation complete, user stories can proceed.

---

## Phase 3: User Story 1 - Manage Listening Content in Admin CMS (Priority: P1)

**Goal**: Deliver flat 4-tab admin CMS (Topics, Exercises, Lessons, Sentences) with CRUD+search, dual-context sentence reorder, and lessons-context JSON import.

**Independent Test**: Admin can use each tab (Topics, Exercises, Lessons, Sentences) for search/CRUD, reorder from Lessons and Sentences contexts, import lesson JSON rows with per-index errors and partial success counts, and learner APIs return updated order.

### Tests for User Story 1

- [ ] T018 [P] [US1] Add admin contract tests for flat tab CRUD/search endpoints (including camelCase filters `topicId`, `exerciseId`, `lessonId`) plus reorder/import contracts (FR-003..FR-011) in `backend/src/test/java/com/khaleo/flashcard/contract/listening/AdminListeningContractTest.java`
- [ ] T019 [P] [US1] Add integration tests for flat list/search APIs with camelCase query filters (`topicId`, `exerciseId`, `lessonId`), scoped slug uniqueness, sentence delete cascade to `user_sentence_progress`, and deterministic reorder persistence (FR-008, FR-009, FR-010, FR-012, FR-043) in `backend/src/test/java/com/khaleo/flashcard/integration/listening/AdminListeningFlatQueryIT.java`
- [ ] T020 [P] [US1] Add integration tests for lessons-context sentence JSON import array payload, per-index errors, and `successCount`/`failedCount` partial success contract (FR-011) in `backend/src/test/java/com/khaleo/flashcard/integration/listening/AdminSentenceImportIT.java`
- [ ] T020a [P] [US1] Add negative integration coverage proving admin save/import paths do not perform media-duration validation calls in phase 1 (FR-017) in `backend/src/test/java/com/khaleo/flashcard/integration/listening/AdminSentenceImportIT.java`
- [ ] T021 [P] [US1] Add frontend admin API contract tests for tab CRUD/search with camelCase filters (`topicId`, `exerciseId`, `lessonId`), both reorder endpoints, and lessons-context import response shape (FR-003..FR-011) in `frontend/src/test/admin/listening/adminListeningApi.contract.test.ts`
- [ ] T022 [P] [US1] Add frontend admin CMS UI tests for 4-tab IA, reorder from Lessons-tab and Sentences-tab contexts, and lessons-context import error rendering (FR-003, FR-010, FR-011) in `frontend/src/test/admin/listening/adminListeningCms.test.tsx`

### Implementation for User Story 1

- [ ] T023 [US1] Implement topic/exercise/lesson/sentence admin DTOs in `backend/src/main/java/com/khaleo/flashcard/controller/admin/listening/dto/{AdminTopicRequest.java,AdminExerciseRequest.java,AdminLessonRequest.java,AdminSentenceRequest.java}`
- [ ] T024 [US1] Implement flat admin CRUD/search service layer for topics/exercises/lessons/sentences using camelCase filters (`topicId`, `exerciseId`, `lessonId`) (FR-003..FR-007) in `backend/src/main/java/com/khaleo/flashcard/service/listening/AdminListeningCrudService.java`
- [ ] T025 [US1] Implement sentence reorder service supporting both Lessons-tab and Sentences-tab endpoint flows (FR-010) in `backend/src/main/java/com/khaleo/flashcard/service/listening/AdminSentenceReorderService.java`
- [ ] T026 [US1] Implement lessons-context sentence JSON import service with array-of-objects parsing, per-index validation errors, and partial success counts (FR-011) in `backend/src/main/java/com/khaleo/flashcard/service/listening/AdminSentenceImportService.java`
- [ ] T027a [US1] Expose Admin Topic CRUD endpoints in `backend/src/main/java/com/khaleo/flashcard/controller/admin/listening/AdminTopicController.java`
- [ ] T027b [US1] Expose Admin Exercise CRUD endpoints in `backend/src/main/java/com/khaleo/flashcard/controller/admin/listening/AdminExerciseController.java`
- [ ] T027c [US1] Expose Admin Lesson CRUD endpoints in `backend/src/main/java/com/khaleo/flashcard/controller/admin/listening/AdminLessonController.java`
- [ ] T027d [US1] Expose Admin Sentence CRUD endpoints in `backend/src/main/java/com/khaleo/flashcard/controller/admin/listening/AdminSentenceController.java`
- [ ] T028 [US1] Add admin listening API client in `frontend/src/features/admin/listening/services/adminListeningApi.ts`
- [ ] T029 [US1] Implement admin listening CMS page shell with flat Topics/Exercises/Lessons/Sentences tabs and shared tab base states (FR-003) in `frontend/src/features/admin/listening/AdminListeningPage.tsx`
- [ ] T030 [US1] Implement sentence reorder entry-point components for Lessons-tab and Sentences-tab contexts in `frontend/src/features/admin/listening/components/{LessonSentenceReorderPanel.tsx,SentenceTabReorderPanel.tsx}`
- [ ] T031 [US1] Implement lessons-context sentence JSON import modal with per-index error rows and partial-success summary in `frontend/src/features/admin/listening/components/SentenceJsonImportModal.tsx`
- [ ] T032 [US1] Mount final AdminListeningPage component into the router and sidebar navigation in `frontend/src/components/Layout.tsx`

**Checkpoint**: US1 is independently functional and testable.

---

## Phase 4: User Story 2 - Complete Dictation Practice in Learner Workspace (Priority: P1)

**Goal**: Deliver strict dictation flow with check/skip-only completion, sentence navigation, keyboard shortcuts, admin access to `/listening`, and translation fallback rendering.

**Independent Test**: Learner (including admin-role user) can open `/listening`, play lesson audio, submit checks/skips, observe completion recorded only for correct-check or skip, and see translation fallback behavior when translation is null.

### Tests for User Story 2

- [x] T033 [P] [US2] Add backend contract tests for learner catalogue/workspace/progress APIs including admin-role access parity on `/listening` data path (FR-002, FR-034, FR-035) in `backend/src/test/java/com/khaleo/flashcard/contract/listening/LearnerListeningContractTest.java`
- [x] T033a [P] [US2] Add frontend route/integration test proving authenticated admin-role users can access learner `/listening` workspace flows (FR-002) in `frontend/src/test/listening/listeningRouteAccess.test.tsx`
- [x] T034 [P] [US2] Add backend integration tests proving completion is written only for `correct_check|skip` actions and persisted in `user_sentence_progress` (FR-023, FR-025) in `backend/src/test/java/com/khaleo/flashcard/integration/listening/LearnerProgressUpdateIT.java`
- [x] T034a [P] [US2] Add explicit negative integration assertions that incorrect Check creates no attempt-history persistence and does not mutate completion state (FR-024) in `backend/src/test/java/com/khaleo/flashcard/integration/listening/LearnerProgressUpdateIT.java`
- [x] T035 [P] [US2] Add frontend unit tests for normalization and exact-match evaluator in `frontend/src/test/listening/dictationEvaluator.test.ts`
- [x] T036 [P] [US2] Add frontend component tests for dictation check/skip/shortcuts and completion indicator updates in `frontend/src/test/listening/listeningDictationFlow.test.tsx`
- [x] T036a [P] [US2] Add frontend component tests for null-translation fallback rendering in dictation/transcript views (FR-014) in `frontend/src/test/listening/listeningTranslationFallback.test.tsx`
- [x] T037 [P] [US2] Add Playwright learner dictation e2e smoke including admin-role learner route access on `/listening` in `frontend/tests/e2e/listening-dictation-core.spec.ts`
- [x] T037a [P] [US2] Add frontend/e2e guard that lesson playback/dictation flow performs no media-duration validation API call in phase 1 (FR-017) in `frontend/tests/e2e/listening-dictation-core.spec.ts`
- [x] T037b [P] [US2] Add backend integration tests for correlation ID propagation + structured logs on learner workspace retrieval and progress update flows (FR-049, FR-050) in `backend/src/test/java/com/khaleo/flashcard/integration/listening/ListeningWorkspaceProgressObservabilityIT.java`

### Implementation for User Story 2

- [x] T038a [US2] Implement learner retrieval DTOs for catalogue in `backend/src/main/java/com/khaleo/flashcard/controller/listening/dto/{TopicListResponse.java,ExerciseListResponse.java}`
- [x] T038b [US2] Implement learner catalogue query service (topics, exercises by `topicId`, lesson by slug) in `backend/src/main/java/com/khaleo/flashcard/service/listening/LearnerCatalogueService.java`
- [x] T038c [US2] Expose learner catalogue endpoints in `backend/src/main/java/com/khaleo/flashcard/controller/listening/LearnerCatalogueController.java`
- [x] T038 [US2] Implement learner workspace response DTOs in `backend/src/main/java/com/khaleo/flashcard/controller/listening/dto/{LessonWorkspaceResponse.java,ProgressUpdateRequest.java}`
- [x] T039 [US2] Implement learner workspace query service in `backend/src/main/java/com/khaleo/flashcard/service/listening/LearnerListeningWorkspaceService.java`
- [x] T040 [US2] Implement progress upsert service enforcing completion on `correct_check|skip` only with no attempt-history persistence (FR-023, FR-024, FR-025) in `backend/src/main/java/com/khaleo/flashcard/service/listening/LearnerSentenceProgressService.java`
- [x] T041 [US2] Expose learner workspace/progress endpoints in `backend/src/main/java/com/khaleo/flashcard/controller/listening/LearnerListeningController.java`
- [x] T041a [US2] Instrument learner workspace/progress services and controller with structured logs + correlation ID propagation (FR-049, FR-050) in `backend/src/main/java/com/khaleo/flashcard/service/listening/{LearnerListeningWorkspaceService.java,LearnerSentenceProgressService.java}` and `backend/src/main/java/com/khaleo/flashcard/controller/listening/LearnerListeningController.java`
- [x] T042 [US2] Implement learner listening API client in `frontend/src/features/listening/services/listeningApi.ts`
- [x] T043 [US2] Implement dictation state hook (answer, check result, skip, current index) in `frontend/src/features/listening/hooks/useDictationSession.ts`
- [x] T043a [US2] Update backend listening security/authorization so admin-role users can access learner `/listening` APIs (FR-002) in `backend/src/main/java/com/khaleo/flashcard/config/SecurityConfig.java`
- [x] T044 [US2] Replace placeholder listening page with core dictation workspace and translation fallback rendering when sentence translation is null (FR-014, FR-026) in `frontend/src/features/listening/ListeningPage.tsx`
- [x] T045 [US2] Implement dictation input panel with check/skip and prev/next controls in `frontend/src/features/listening/components/DictationTab.tsx`
- [x] T046 [US2] Implement progress header and sentence counter in `frontend/src/features/listening/components/ListeningProgressHeader.tsx`

**Checkpoint**: US2 is independently functional and testable.

---

## Phase 5: User Story 3 - Use Transcript, Dictionary, and Settings for Better Learning (Priority: P2)

**Goal**: Deliver transcript playback controls, dictionary popup via backend proxy, and user settings for replay/auto-play behavior.

**Independent Test**: Learner can use full transcript click-to-play, auto-scroll, loop/repeat, open dictionary popup on word click, and continue studying when dictionary provider fails.

### Tests for User Story 3

- [x] T047 [P] [US3] Add backend contract tests for media access and dictionary proxy endpoints in `backend/src/test/java/com/khaleo/flashcard/contract/listening/ListeningDictionaryMediaContractTest.java`
- [x] T048 [P] [US3] Add backend integration tests for dictionary failure fallback payload in `backend/src/test/java/com/khaleo/flashcard/integration/listening/DictionaryProxyFallbackIT.java`
- [x] T047a [P] [US3] Add backend integration tests for media access TTL boundaries (accept 60s/300s; reject <60s and >300s) (FR-018, FR-036) in `backend/src/test/java/com/khaleo/flashcard/integration/listening/MediaAccessTtlBoundaryIT.java`
- [x] T048a [P] [US3] Add backend integration tests for dictionary provider host allowlist enforcement and bounded outbound timeout failures (FR-045, FR-046) in `backend/src/test/java/com/khaleo/flashcard/integration/listening/DictionaryProxySecurityIT.java`
- [x] T048b [P] [US3] Add backend integration tests for dictionary term sanitization/validation with controlled rejection responses (FR-047) in `backend/src/test/java/com/khaleo/flashcard/integration/listening/DictionaryProxySecurityIT.java`
- [x] T048c [P] [US3] Add backend integration tests for dictionary response-size cap and rate-limit abuse controls (FR-048) in `backend/src/test/java/com/khaleo/flashcard/integration/listening/DictionaryProxySecurityIT.java`
- [x] T048d [P] [US3] Add backend integration tests for correlation ID propagation + structured logs across media access and dictionary flows (FR-049, FR-050) in `backend/src/test/java/com/khaleo/flashcard/integration/listening/ListeningMediaDictionaryObservabilityIT.java`
- [x] T049 [P] [US3] Add frontend unit tests for transcript auto-scroll and loop logic in `frontend/src/test/listening/transcriptPlayback.test.ts`
- [x] T049a [P] [US3] Add frontend playback-orchestration tests proving `Auto Play on Next` triggers playback only on sentence transition when enabled and stays idle when disabled (FR-027) in `frontend/src/test/listening/autoPlayOnNextTransition.test.ts`
- [x] T049b [P] [US3] Add frontend playback-loop runtime tests proving `replayCount` and `replayInterval` are executed per sentence transition (None/1/2/Infinite x 0.5s/1.0s/1.5s) (FR-029) in `frontend/src/test/listening/autoReplayLoopRuntime.test.ts`
- [x] T050 [P] [US3] Add frontend component tests for settings modal persistence and shortcuts overrides in `frontend/src/test/listening/listeningSettingsModal.test.tsx`
- [x] T051 [P] [US3] Add frontend component tests for dictionary popup success/fallback UX in `frontend/src/test/listening/dictionaryPopup.test.tsx`
- [x] T052 [P] [US3] Add Playwright e2e for transcript + dictionary + settings flow in `frontend/tests/e2e/listening-transcript-dictionary.spec.ts`
- [x] T052a [P] [US3] Extend Playwright learner e2e to verify sentence-transition auto-play and replay-loop runtime behavior from settings in live playback flow (FR-027, FR-029) in `frontend/tests/e2e/listening-transcript-dictionary.spec.ts`

### Implementation for User Story 3

- [x] T053 [US3] Implement media access request/response DTOs in `backend/src/main/java/com/khaleo/flashcard/controller/listening/dto/{MediaAccessRequest.java,MediaAccessResponse.java}`
- [x] T053a [US3] Enforce media access TTL validation (60-300 seconds inclusive) and controlled out-of-range rejection responses (FR-018, FR-036) in `backend/src/main/java/com/khaleo/flashcard/service/listening/ListeningMediaAccessService.java`
- [x] T054 [US3] Implement dictionary proxy endpoint contract DTOs in `backend/src/main/java/com/khaleo/flashcard/controller/listening/dto/{DictionaryLookupResponse.java,DictionaryLookupFailureResponse.java}`
- [x] T054a [US3] Implement dictionary term sanitization/validation policy (trim, length bounds, character checks) before proxy calls (FR-047) in `backend/src/main/java/com/khaleo/flashcard/service/listening/DictionaryTermValidationService.java`
- [x] T055 [US3] Expose media access and dictionary endpoints in `backend/src/main/java/com/khaleo/flashcard/controller/listening/ListeningSupportController.java`
- [x] T055a [US3] Add correlation ID propagation and structured logging for media access/dictionary request lifecycle and failures (FR-049, FR-050) in `backend/src/main/java/com/khaleo/flashcard/controller/listening/ListeningSupportController.java`
- [x] T056 [US3] Implement dictionary cache repository in `backend/src/main/java/com/khaleo/flashcard/repository/DictionaryCacheRepository.java` and service in `backend/src/main/java/com/khaleo/flashcard/service/listening/DictionaryCacheService.java`
- [x] T056a [US3] Enforce dictionary outbound host allowlist and timeout policy in proxy client/service configuration (FR-045, FR-046) in `backend/src/main/java/com/khaleo/flashcard/service/listening/DictionaryProxyService.java` and `backend/src/main/resources/application.yml`
- [x] T056b [US3] Enforce dictionary response-size limits and lookup abuse throttling/rate limiting controls (FR-048) in `backend/src/main/java/com/khaleo/flashcard/service/listening/DictionaryProxyService.java` and `backend/src/main/java/com/khaleo/flashcard/config/RateLimitConfig.java`
- [x] T057 [US3] Implement full transcript tab with click-to-play, auto-scroll, and loop controls in `frontend/src/features/listening/components/FullTranscriptTab.tsx`
- [x] T058 [US3] Implement settings modal UI and localStorage sync in `frontend/src/features/listening/components/ListeningSettingsModal.tsx`
- [x] T058a [US3] Implement sentence-transition orchestration that applies `Auto Play on Next` at runtime when advancing after Check/Skip/Next actions (FR-027) in `frontend/src/features/listening/hooks/useDictationSession.ts`
- [x] T058b [US3] Implement runtime playback loop executor that applies `replayCount` and `replayInterval` values for each active sentence playback cycle (FR-029) in `frontend/src/features/listening/hooks/useListeningAudioPlayer.ts`
- [x] T059 [US3] Implement clickable word dictionary popup component in `frontend/src/features/listening/components/DictionaryPopover.tsx`
- [x] T060 [US3] Integrate transcript/settings/dictionary into listening workspace shell and wire runtime playback behavior from settings (`autoPlayOnNext`, `replayCount`, `replayInterval`) in `frontend/src/features/listening/ListeningPage.tsx`

**Checkpoint**: US3 is independently functional and testable.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Hardening, documentation, and release-readiness validation across all stories.

- [x] T061 [P] Update feature quickstart troubleshooting and verification notes in `specs/003-listening-dictation/quickstart.md`
- [x] T062 [P] Align OpenAPI details with implemented payloads/codes in `specs/003-listening-dictation/contracts/listening-dictation.openapi.yaml`
- [x] T063 [P] Document listening learner/admin usage and env variables in `frontend/README.md` and `backend/README.md`
- [x] T063a [P] Verify/enforce/document FR-022 governance so `Dictation Normalization & Matching Rules` are updated whenever evaluator logic/tests change in `specs/003-listening-dictation/spec.md` and `specs/003-listening-dictation/quickstart.md`
- [x] T064 [P] Add manual local e2e checklist for listening workflows in `docs/manual-e2e/phase3-listening-local-checklist.md`
- [x] T065 Run backend listening regression suite in `backend/src/test/java/com/khaleo/flashcard/integration/listening/ListeningFeatureRegressionIT.java`
- [x] T066 [P] Run frontend unit and e2e listening suites and capture outcomes in `docs/manual-e2e/phase3-listening-local-checklist.md`
- [x] T067 [P] Measure and document performance evidence against plan p95 targets (learner APIs, UI reaction, reorder/import latency) in `docs/manual-e2e/phase3-listening-local-checklist.md`
- [x] T067a [P] If any PE target fails in T067, document root-cause analysis and remediation plan (owner, milestone, validation method) in `docs/manual-e2e/phase3-listening-local-checklist.md`
- [x] T067b If any PE target remains above budget after remediation, record constitution-required approval/exception (approver, scope, expiry, follow-up task IDs) in `docs/manual-e2e/phase3-listening-local-checklist.md`

`SC-002` note: treat as post-build UAT business metric; do not enforce as blocking automated CI/build gate.

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

- `US1 (P1)` -> establishes flat 4-tab admin CRUD/search authoring and sentence reorder/import pipeline.
- `US2 (P1)` -> depends on foundational schema/services and requires published lesson/sentence data from US1.
- `US3 (P2)` -> depends on US2 learner workspace baseline and adds transcript/dictionary/settings enhancements.

### FR Traceability Highlights

- FR-002 -> `T033`, `T033a`, `T043a`, `T037`.
- FR-010 -> `T019`, `T021`, `T022`, `T025`, `T030`.
- FR-011 -> `T018`, `T020`, `T021`, `T022`, `T026`, `T031`.
- FR-012 & FR-043 -> `T006`, `T019`.
- FR-014 -> `T036a`, `T044`.
- FR-017 -> `T011`, `T020a`, `T037a`.
- FR-018 & FR-036 -> `T047a`, `T053a`.
- FR-022 -> `T063a`.
- FR-023, FR-024, FR-025 -> `T034`, `T034a`, `T040`.
- FR-027 -> `T049a`, `T052a`, `T058a`, `T060`.
- FR-029 -> `T049b`, `T052a`, `T058b`, `T060`.
- FR-045, FR-046, FR-047, FR-048 -> `T048a`, `T048b`, `T048c`, `T054a`, `T056a`, `T056b`.
- FR-049, FR-050 -> `T017a`, `T017b`, `T037b`, `T041a`, `T048d`, `T055a`.

### PE Traceability Highlights

- PE-001 -> `T067`, `T067a`, `T067b`.
- PE-002 -> `T067`, `T067a`, `T067b`.
- PE-003 -> `T067`, `T067a`, `T067b`.

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
- US2 tests `T033-T037b` can run in parallel.
- US3 tests `T047-T052a` plus `T047a`, `T048a-T048d`, `T049a`, and `T049b` can run in parallel (except shared-file tasks in `DictionaryProxySecurityIT.java` and `listening-transcript-dictionary.spec.ts`).
- Phase 6 performance mitigation/docs tasks `T067a` and `T067b` can run after `T067` captures measured evidence.
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
T030 frontend/src/features/admin/listening/components/LessonSentenceReorderPanel.tsx
```

## Parallel Example: User Story 2

```bash
# Parallel tests
T035 frontend/src/test/listening/dictationEvaluator.test.ts
T036 frontend/src/test/listening/listeningDictationFlow.test.tsx
T036a frontend/src/test/listening/listeningTranslationFallback.test.tsx
T037 frontend/tests/e2e/listening-dictation-core.spec.ts
T037b backend/src/test/java/com/khaleo/flashcard/integration/listening/ListeningWorkspaceProgressObservabilityIT.java

# Parallel implementation slices
T039 backend/src/main/java/com/khaleo/flashcard/service/listening/LearnerListeningWorkspaceService.java
T040 backend/src/main/java/com/khaleo/flashcard/service/listening/LearnerSentenceProgressService.java
T045 frontend/src/features/listening/components/DictationTab.tsx
```

## Parallel Example: User Story 3

```bash
# Parallel tests
T049 frontend/src/test/listening/transcriptPlayback.test.ts
T049a frontend/src/test/listening/autoPlayOnNextTransition.test.ts
T049b frontend/src/test/listening/autoReplayLoopRuntime.test.ts
T050 frontend/src/test/listening/listeningSettingsModal.test.tsx
T051 frontend/src/test/listening/dictionaryPopup.test.tsx
T047a backend/src/test/java/com/khaleo/flashcard/integration/listening/MediaAccessTtlBoundaryIT.java
T048d backend/src/test/java/com/khaleo/flashcard/integration/listening/ListeningMediaDictionaryObservabilityIT.java

# Parallel implementation slices
T055 backend/src/main/java/com/khaleo/flashcard/controller/listening/ListeningSupportController.java
T056a backend/src/main/java/com/khaleo/flashcard/service/listening/DictionaryProxyService.java
T058a frontend/src/features/listening/hooks/useDictationSession.ts
T058b frontend/src/features/listening/hooks/useListeningAudioPlayer.ts
T057 frontend/src/features/listening/components/FullTranscriptTab.tsx
T059 frontend/src/features/listening/components/DictionaryPopover.tsx
```

---

## Implementation Strategy

### Phase 1 Delivery Baseline (FR-001)

1. Complete Phase 1 setup.
2. Complete Phase 2 foundational layer.
3. Deliver Phase 3 (US1) admin CMS and validate independently.
4. Deliver Phase 4 (US2) learner dictation core and validate independently.
5. Deliver Phase 5 (US3) learner transcript/dictionary/settings capabilities and complete phase-1 acceptance.

### Incremental Delivery

1. Add US2 strict dictation learner flow after admin content foundation is ready.
2. Add US3 transcript/dictionary/settings enhancements on top of US2 baseline.
3. Finish with Phase 6 cross-cutting hardening, docs, and regression runs.

### Suggested Phase-1 Scope

- Phase-1 release scope (FR-001): **Phase 1 + Phase 2 + Phase 3 (US1) + Phase 4 (US2) + Phase 5 (US3)**.
- Internal rollout preference (without changing scope): validate in sequence **US1 -> US2 -> US3** before final phase-1 sign-off.

---

## Notes

- All tasks follow required checklist format: `- [ ] Txxx [P?] [US?] Description with file path`.
- Story labels are included only on user story tasks.
- `[P]` marks tasks designed to avoid same-file conflicts and allow parallel execution.
- Task IDs follow the format `Txxx`. Alpha-suffixes (e.g., `T027a`, `T027b`) are explicitly permitted in this project to denote sub-tasks belonging to a logical group without requiring a full renumbering of the sequence.
