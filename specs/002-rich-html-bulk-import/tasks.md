# Tasks: Rich HTML Card Model + Bulk Card Import in Add Card Modal

**Input**: Design documents from `specs/002-rich-html-bulk-import/`
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/card-rich-content.openapi.yaml`, `quickstart.md`

**Tests**: Tests are included because the feature spec and quickstart explicitly require backend/frontend contract, unit/integration, and e2e coverage.

**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare dependencies and feature scaffolding for canonical HTML + phase-1 bulk chunk orchestration.

- [X] T001 Add backend HTML sanitization dependency configuration in `backend/pom.xml`
- [X] T002 [P] Add TipTap and frontend sanitizer dependencies in `frontend/package.json`
- [X] T003 [P] Create feature sanitizer/parser/chunking constants including `MAX_BULK_CHUNK_SIZE = 500` in `frontend/src/features/cards/config/richCardConfig.ts`
- [X] T004 [P] Add frontend bulk import orchestration state type definitions in `frontend/src/features/cards/types/bulkImportOrchestration.ts`
- [X] T005 [P] Add backend rich-card feature flags and allowlist defaults in `backend/src/main/resources/application.yml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Introduce canonical schema and shared backend/frontend primitives required by all stories.

**CRITICAL**: No user story implementation starts until this phase is complete.

- [X] T006 Create canonical schema migration for `front_content`, `back_content`, and `search_text` in `backend/src/main/resources/db/migration/V20260417_015__canonical_html_bulk_import.sql`
- [X] T007 [P] Add migration/backfill integration verification for canonical columns in `backend/src/test/java/com/khaleo/flashcard/integration/RichHtmlCanonicalMigrationIT.java`
- [X] T008 Update JPA canonical fields and lifecycle normalization in `backend/src/main/java/com/khaleo/flashcard/entity/Card.java`
- [X] T009 [P] Add backend HTML sanitizer allowlist service for STRIP behavior in `backend/src/main/java/com/khaleo/flashcard/service/persistence/CardHtmlSanitizer.java`
- [X] T010 [P] Add HTML-to-plain-text index builder for search persistence in `backend/src/main/java/com/khaleo/flashcard/service/persistence/CardSearchTextBuilder.java`
- [X] T011 [P] Add bulk row error code enum and API model in `backend/src/main/java/com/khaleo/flashcard/controller/card/dto/BulkRowErrorCode.java`
- [X] T012 [P] Add frontend render-time sanitizer utility aligned with backend policy in `frontend/src/features/cards/utils/sanitizeRichHtml.ts`
- [X] T013 [P] Add bulk parser domain types and validation result models in `frontend/src/features/cards/types/bulkImport.ts`

**Checkpoint**: Foundation ready for story implementation.

---

## Phase 3: User Story 1 - Create and study rich cards (Priority: P1) 🎯 MVP

**Goal**: Users create/update canonical rich HTML cards and study renders sanitized rich content safely.

**Independent Test**: Create and update cards using only `frontContent`/`backContent`, then run study flow to confirm safe rich rendering.

### Tests for User Story 1

- [X] T014 [P] [US1] Add canonical create/update contract coverage in `backend/src/test/java/com/khaleo/flashcard/contract/RichCardContractTest.java`
- [X] T015 [P] [US1] Add sanitizer and media allowlist unit tests in `backend/src/test/java/com/khaleo/flashcard/unit/persistence/CardHtmlSanitizerTest.java`
- [X] T016 [P] [US1] Add canonical persistence integration test for create/update/search_text sync in `backend/src/test/java/com/khaleo/flashcard/integration/RichCardCanonicalPersistenceIT.java`
- [X] T017 [P] [US1] Add frontend rich-card API contract test updates in `frontend/src/services/contracts/richCard.contract.test.ts`
- [X] T018 [P] [US1] Add study rich HTML render safety tests in `frontend/src/test/study-session/richCardSanitizedRender.test.tsx`

### Implementation for User Story 1

- [X] T019 [US1] Replace single-card create payload with canonical fields in `backend/src/main/java/com/khaleo/flashcard/controller/card/dto/CreateCardRequest.java`
- [X] T020 [US1] Replace single-card PUT payload with required canonical fields in `backend/src/main/java/com/khaleo/flashcard/controller/card/dto/UpdateCardRequest.java`
- [X] T021 [US1] Return canonical card content fields in `backend/src/main/java/com/khaleo/flashcard/controller/card/dto/CardResponse.java`
- [X] T022 [US1] Refactor create/update validation and sanitize-before-save flow in `backend/src/main/java/com/khaleo/flashcard/service/persistence/RelationalPersistenceService.java`
- [X] T023 [US1] Update single-card controller mapping to canonical DTO contracts in `backend/src/main/java/com/khaleo/flashcard/controller/card/CardController.java`
- [X] T024 [US1] Update card search query to use derived `search_text` matching in `backend/src/main/java/com/khaleo/flashcard/repository/CardRepository.java`
- [X] T025 [US1] Add canonical card frontend types for API and study state in `frontend/src/types.ts`
- [X] T026 [US1] Update study session API mapping to canonical fields in `frontend/src/services/studySessionApi.ts`
- [X] T027 [US1] Render sanitized canonical rich HTML in study card surface in `frontend/src/components/StudyCardImage.tsx`

**Checkpoint**: US1 is independently functional and testable (MVP).

---

## Phase 4: User Story 2 - Import many cards quickly from spreadsheet text (Priority: P1)

**Goal**: Users paste spreadsheet text, preview parsed rows, submit chunked bulk create requests, and recover from chunk failures without losing successful chunks.

**Independent Test**: Paste input with >500 candidate rows, verify sequential chunk execution with `saved X/Y`, force a mid-run request failure, verify fail-stop + failed line range, then retry from failed chunk without re-sending successful chunks.

### Tests for User Story 2

- [X] T028 [P] [US2] Add bulk endpoint contract tests for HTTP 200 partial success and HTTP 400 request-level `cards.length > 500` guard in `backend/src/test/java/com/khaleo/flashcard/contract/BulkCardImportContractTest.java`
- [X] T029 [P] [US2] Add bulk service integration tests for count invariants and original line passthrough in `backend/src/test/java/com/khaleo/flashcard/integration/BulkCardImportIntegrationIT.java`
- [X] T030 [P] [US2] Add frontend bulk API contract tests for chunk request envelope and >500 guard handling in `frontend/src/services/contracts/cardBulkApi.contract.test.ts`
- [X] T031 [P] [US2] Add chunk utility unit tests for max-500 boundaries and line-range mapping in `frontend/src/test/cards/bulkChunking.test.ts`
- [X] T032 [P] [US2] Add orchestrator unit tests for strict sequential dispatch (no parallel chunk requests) in `frontend/src/test/cards/bulkChunkOrchestrator.sequential.test.ts`
- [X] T033 [P] [US2] Add orchestrator unit tests for fail-stop, failed chunk range capture, and retry-from-failed-index behavior in `frontend/src/test/cards/bulkChunkOrchestrator.failure-retry.test.ts`
- [X] T034 [P] [US2] Add Add Card modal component tests for cumulative progress label `saved X/Y` and halted-state messaging in `frontend/src/test/cards/addCardModal.bulkProgress.test.tsx`
- [X] T035 [P] [US2] Add Playwright e2e test for multi-chunk happy path (>500 rows) with progress verification in `frontend/src/test/e2e/bulk-import-multi-chunk.spec.ts`
- [X] T036 [P] [US2] Add Playwright e2e test for middle-chunk failure halt and retry-from-failed-range resume in `frontend/src/test/e2e/bulk-import-failure-retry.spec.ts`

### Implementation for User Story 2

- [X] T037 [US2] Add bulk request/response DTOs and standardized row error model in `backend/src/main/java/com/khaleo/flashcard/controller/card/dto/BulkCreateCardsRequest.java`
- [X] T038 [US2] Implement partial-commit bulk create service with row validation in `backend/src/main/java/com/khaleo/flashcard/service/persistence/BulkCardImportService.java`
- [X] T039 [US2] Expose `POST /api/v1/decks/{deckId}/cards/bulk` endpoint in `backend/src/main/java/com/khaleo/flashcard/controller/card/CardController.java`
- [X] T040 [US2] Enforce max-500 request guard and standardized row error mapping in `backend/src/main/java/com/khaleo/flashcard/service/persistence/PersistenceValidationExceptionMapper.java`
- [X] T041 [US2] Add frontend bulk API client and transport error classification for chunk-level failures in `frontend/src/services/cardBulkApi.ts`
- [X] T042 [US2] Implement line-aware bulk parser with tab/comma modes in `frontend/src/features/cards/utils/parseBulkCardInput.ts`
- [X] T043 [US2] Implement chunking utility that splits candidates into <=500-card chunks with original line ranges in `frontend/src/features/cards/utils/buildBulkChunks.ts`
- [X] T044 [US2] Implement sequential chunk orchestrator state machine in `frontend/src/features/cards/services/runBulkChunkImport.ts`
- [X] T045 [US2] Extend Add Card modal store with chunk-run progress (`saved X/Y`), failed chunk metadata, and retry cursor state in `frontend/src/store/addCardModalStore.ts`
- [X] T046 [US2] Add bulk progress and failed chunk range UI (`lines start-end`) with retry action wiring in `frontend/src/features/cards/components/BulkImportProgressBanner.tsx`
- [X] T047 [US2] Upgrade Add Card modal bulk flow to integrate parser, orchestrator, fail-stop behavior, and retry-from-failed-chunk execution without rollback of prior chunks in `frontend/src/components/AddCardModal.tsx`

**Checkpoint**: US2 is independently functional and testable.

---

## Phase 5: User Story 3 - Moderate and find cards after model change (Priority: P2)

**Goal**: Admins moderate canonical rich cards with WYSIWYG editing, and search remains effective via plain-text indexing.

**Independent Test**: Admin edits rich content in moderation UI and saves; search still matches visible text from canonical HTML.

### Tests for User Story 3

- [X] T048 [P] [US3] Add admin card update contract tests for canonical rich payloads in `backend/src/test/java/com/khaleo/flashcard/contract/admin/AdminCardModerationRichContentContractTest.java`
- [X] T049 [P] [US3] Add admin moderation integration test for search_text refresh after update in `backend/src/test/java/com/khaleo/flashcard/integration/AdminCardModerationSearchIndexIT.java`
- [X] T050 [P] [US3] Add frontend WYSIWYG moderation behavior tests in `frontend/src/test/admin/adminCardsRichEditor.test.tsx`

### Implementation for User Story 3

- [X] T051 [US3] Switch admin card update DTO to canonical rich content fields in `backend/src/main/java/com/khaleo/flashcard/controller/admin/dto/AdminCardUpdateRequest.java`
- [X] T052 [US3] Update admin moderation update flow to sanitize and persist canonical/search_text fields in `backend/src/main/java/com/khaleo/flashcard/service/admin/AdminModerationService.java`
- [X] T053 [US3] Add TipTap-based admin card editor in `frontend/src/features/admin/cards/AdminCardsPage.tsx`
- [X] T054 [US3] Update admin API contracts for canonical card update payload in `frontend/src/services/adminApi.ts`
- [X] T055 [US3] Add shared rich-text editor wrapper for Add Card and admin moderation in `frontend/src/features/cards/components/RichTextEditor.tsx`

**Checkpoint**: US3 is independently functional and testable.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Hardening, docs, and end-to-end verification across stories.

- [X] T056 [P] Update feature quickstart verification steps and chunk-failure retry walkthrough in `specs/002-rich-html-bulk-import/quickstart.md`
- [X] T057 [P] Align OpenAPI contract examples/codes with implemented bulk response and request guard semantics in `specs/002-rich-html-bulk-import/contracts/card-rich-content.openapi.yaml`
- [X] T058 [P] Document frontend chunking execution model and troubleshooting notes in `frontend/README.md`
- [X] T059 Run backend full regression suite including rich/bulk/chunk guard tests in `backend/src/test/java/com/khaleo/flashcard/integration/FeaturePerformanceValidationIT.java`
- [X] T060 [P] Run frontend unit + e2e suites and capture acceptance checklist updates in `docs/manual-e2e/phase1-local-checklist.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- Phase 1 -> required before Phase 2.
- Phase 2 -> blocks all user stories.
- Phase 3 (US1) -> can start after Phase 2.
- Phase 4 (US2) -> can start after Phase 2 and depends on canonical contracts from US1 for shared DTO/UI fields.
- Phase 5 (US3) -> depends on Phase 2 and US1 canonical model/search_text flow.
- Phase 6 -> depends on completed stories targeted for release.

### User Story Dependency Graph

- `US1 (P1)` -> establishes canonical model + study rendering baseline.
- `US2 (P1)` -> depends on foundational primitives and consumes canonical contract shape from US1.
- `US3 (P2)` -> depends on canonical model and search-text synchronization from US1.

### Within-Story Execution Rules

- Write story tests first and confirm they fail before implementation.
- Backend DTO/entity/service updates before controller wiring.
- For US2, complete chunk utility/orchestrator/store wiring before modal UI retry behavior.
- Complete each story checkpoint before moving release scope forward.

### Parallel Opportunities

- Setup tasks T002-T005 can run concurrently.
- Foundational tasks T007, T009-T013 can run concurrently after T006 starts.
- US1 tests T014-T018 can run in parallel.
- US2 tests T028-T036 can run in parallel.
- US2 implementation backend track (T037-T040) can run in parallel with frontend parser/chunk utility track (T042-T043), then converge at T044-T047.
- US3 tests T048-T050 can run in parallel.

---

## Parallel Example: User Story 2

```bash
# Parallel tests (before implementation)
T031 frontend/src/test/cards/bulkChunking.test.ts
T032 frontend/src/test/cards/bulkChunkOrchestrator.sequential.test.ts
T035 frontend/src/test/e2e/bulk-import-multi-chunk.spec.ts

# Parallel implementation slices after API DTO baseline exists
T038 backend/src/main/java/com/khaleo/flashcard/service/persistence/BulkCardImportService.java
T042 frontend/src/features/cards/utils/parseBulkCardInput.ts
T043 frontend/src/features/cards/utils/buildBulkChunks.ts
```

---

## Implementation Strategy

### MVP First (US1)

1. Complete Phase 1 and Phase 2.
2. Deliver Phase 3 (US1) end-to-end (canonical create/update + study rendering + sanitizer).
3. Validate US1 independently via T014-T018 before expanding scope.

### Incremental Delivery

1. Add US2 chunked bulk import flow with fail-stop and targeted retry after US1 contracts are stable.
2. Add US3 moderation/search continuity after canonical indexing is validated.
3. Finish with Phase 6 cross-cutting verification and documentation alignment.

### Suggested MVP Scope

- MVP release scope: **Phase 1 + Phase 2 + Phase 3 (US1 only)**.
- Follow-up release scope: **US2 chunked bulk import**, then **US3 moderation/search refinements**.

---

## Notes

- All tasks follow the required checklist format: `- [ ] Txxx [P?] [US?] Description with file path`.
- `[P]` marks tasks that can run in parallel with no unresolved file dependency conflicts.
- User story labels are present on all story-phase tasks and omitted for setup/foundational/polish phases.

