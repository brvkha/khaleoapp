# Implementation Plan: Rich HTML Card Model + Bulk Card Import in Add Card Modal

**Branch**: `002-rich-html-bulk-import` | **Date**: 2026-04-17 | **Spec**: `/specs/002-rich-html-bulk-import/spec.md`
**Input**: Feature specification from `/specs/002-rich-html-bulk-import/spec.md`

## Summary

Migrate card authoring and storage to canonical sanitized HTML fields (`frontContent`, `backContent`), add a partial-success bulk create API (`POST /api/v1/decks/{deckId}/cards/bulk`), and update Add Card modal UX to support preview-first bulk import. The implementation keeps legacy DB fields for rollback safety, applies dual sanitization (backend at write-time, frontend at render-time), preserves line-aware bulk validation feedback, and maintains search behavior through plain-text indexing derived from canonical HTML.

Bulk import execution is explicitly split across two layers: backend enforces a hard request-level guard of `cards.length <= 500` per API call, while frontend supports imports above 500 total rows by pre-parsing all rows and sending sequential chunks of up to 500 until completion or first request-level failure.

## Technical Context

**Language/Version**: Java 17 (Spring Boot 3.3.x), TypeScript 5.9 + React 19  
**Primary Dependencies**: Spring Web/Data JPA/Validation, Flyway, MySQL connector, React + Zustand, Vite, sanitizer stack (backend HTML sanitizer + frontend DOM sanitizer), lightweight WYSIWYG editor (TipTap selected in research)  
**Storage**: MySQL relational DB (`cards` canonical HTML columns + derived searchable plain-text column), localStorage (last Add Card tab), existing media allowlist config  
**Testing**: JUnit 5 + Spring Boot tests (backend), Vitest + React Testing Library (frontend), Playwright smoke/e2e for modal workflow  
**Target Platform**: Web application (browser frontend + Spring Boot backend API)
**Project Type**: Monorepo web application (`frontend/` + `backend/`)  
**Performance Goals**: Bulk preview renders max 100 rows per spec, request cap 500 cards, no per-row rich editor instantiation, row-level validation and commit remain responsive for typical deck workflows  
**Constraints**: STRIP sanitizer policy, safe-tag/attribute allowlist, no inline style, media URLs must pass existing allowlist, bulk endpoint returns HTTP 200 for full/partial success, canonical update is `PUT` only, no PATCH/idempotency guarantee in phase 1, request-level limit remains 500 cards while frontend supports multi-request orchestration for larger totals  
**Scale/Scope**: Existing card CRUD and study/search/moderation flows plus Add Card modal changes; bulk import request size fixed to 1..500 cards with line mapping including blank lines, with client-side sequential chunking for larger overall imports

## Chunking Behavior Alignment

- **Backend invariant (unchanged)**: `POST /api/v1/decks/{deckId}/cards/bulk` accepts only 1..500 cards per request and rejects `cards.length > 500` with HTTP 400 and no processing for that request.
- **Frontend capability (unchanged)**: Add Card bulk flow supports total imports above 500 rows (for example 3000) by splitting into sequential request chunks of at most 500.
- **Execution model**: A chunk that returns HTTP 200 (full or partial success) is considered completed, contributes to cumulative counters, and allows the next chunk to start.
- **Failure boundary**: A request-level failure (network error, timeout, non-2xx, or backend 400/500) halts the run immediately; already completed chunks remain committed.

## Frontend Chunk Orchestrator Design

### State Machine

The bulk submit flow uses a deterministic client-side state machine to avoid parallel dispatch and preserve replay clarity:

1. `IDLE`: User can edit input, choose separator, and run preview parse.
2. `READY_TO_SUBMIT`: Parse/validation complete, candidate rows and chunk plan frozen for this run.
3. `SUBMITTING_CHUNK`: Exactly one in-flight request, addressed by `currentChunkIndex`.
4. `CHUNK_COMPLETED`: Chunk result persisted in aggregate counters and error map; transition to next chunk or terminal success.
5. `HALTED_ON_FAILURE`: Request-level failure captured with failed chunk metadata; no subsequent chunks dispatched.
6. `COMPLETED`: All chunks processed; terminal success (full) or terminal partial success.

### Orchestrator Data Model

- `runId`: unique identifier for one submit run to ignore stale async updates.
- `chunks`: immutable array of chunk descriptors `{ index, startLine, endLine, cards[] }`.
- `currentChunkIndex`: zero-based pointer of active chunk.
- `totals`: `{ totalCandidates, savedCount, failedCount }` where `savedCount` is cumulative successful row writes.
- `rowErrorsByLine`: normalized map keyed by original textarea `line`.
- `failedChunk`: optional `{ index, startLine, endLine, reason }` captured only on request-level failure.
- `status`: one of state-machine statuses above.

### Progress Reporting

- Progress label is rendered as `saved X/Y` where:
  - `Y = totals.totalCandidates` (all candidate rows included in planned chunks)
  - `X = totals.savedCount` (sum of `successCount` from completed chunks)
- `X` updates only after each chunk response finalizes to avoid optimistic drift.
- Per-chunk partial successes increment both `savedCount` and `failedCount` using API response counts.

### Fail-Stop Policy

- Any request-level failure transitions `status` to `HALTED_ON_FAILURE` and blocks dispatch of `currentChunkIndex + 1`.
- UI preserves outcomes and row-level errors from completed chunks.
- Failure UI shows failed chunk context using original lines: `lines {startLine}-{endLine}`.

### Retry From Failed Chunk Range

- Retry is user-initiated and scoped from the failed chunk onward.
- Orchestrator builds a new run plan using unsubmitted chunks beginning at `failedChunk.index`.
- Previously completed chunks are not resent in the same retry action and remain committed.
- Because phase 1 has no idempotency guarantee, UI warns that manual full-run retry may create duplicates.

## Testing Strategy (Chunked Import Focus)

### Frontend Unit/Component Tests (Vitest + RTL)

- Chunk planner tests: split boundaries for totals `<=500`, `501`, `999`, `1000`, and original line-range preservation including blank lines.
- State machine tests: valid transitions only (`READY_TO_SUBMIT -> SUBMITTING_CHUNK -> CHUNK_COMPLETED/...`) and no parallel in-flight chunk requests.
- Progress tests: cumulative `saved X/Y` updates per completed chunk including partial-success responses.
- Fail-stop tests: simulated timeout/network/server failure on chunk `n` halts chunk `n+1...` dispatch and surfaces failed range.
- Retry-scope tests: retry starts at failed index and excludes previously completed chunks.

### Backend/API Contract Tests (JUnit + Spring)

- Request-size guard test: `cards.length > 500` returns HTTP 400 and processes zero rows for that request.
- Partial success test: HTTP 200 response with consistent `successCount`, `failedCount`, and standardized row errors.
- Line-mapping test: row errors retain original textarea line numbers passed from client.

### Integration/E2E Tests (Playwright)

- Multi-chunk happy path: paste >500 rows, verify sequential processing and terminal success behavior.
- Multi-chunk mixed validity: verify cumulative progress, partial failures, and line-level highlighting.
- Timeout/network failure path: force failure on a middle chunk, verify halt behavior, committed prior chunks, and retry context display.
- Recovery path: retry from failed range and verify resumed sequential completion without resending earlier chunks.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- Constitution source (`.specify/memory/constitution.md`) is a placeholder template with no ratified enforceable principles; no explicit gate violations are currently derivable.
- Interim gates applied from this feature spec and repository standards:
  - Security gate: dual sanitization + domain allowlist enforcement required.
  - Contract gate: canonical card payload semantics and bulk response schema must remain stable.
  - Quality gate: add/update coverage for parser, sanitizer behavior, and partial-success endpoint contract.
- **Gate status before Phase 0**: PASS (no unresolved clarifications; controls defined in spec and plan).
- **Gate status after Phase 1 design**: PASS (research/data model/contracts/quickstart satisfy planned controls; no additional complexity exception required).

## Project Structure

### Documentation (this feature)

```text
specs/002-rich-html-bulk-import/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── card-rich-content.openapi.yaml
└── tasks.md             # Created by /speckit.tasks (not by this command)
```

### Source Code (repository root)
```text
backend/
├── src/
│   ├── main/java/com/khaleo/flashcard/
│   │   ├── controller/card/            # Single-card + bulk-card endpoints
│   │   ├── controller/card/dto/        # Canonical request/response contracts
│   │   ├── service/persistence/        # Canonical validation + bulk create flow
│   │   ├── entity/                     # Card canonical/search fields
│   │   └── repository/                 # Bulk persistence and search queries
│   └── main/resources/db/migration/    # Canonical field + search-index migrations
└── src/test/                           # Parser/sanitizer/contract tests

frontend/
├── src/
│   ├── components/                     # Add Card modal tabs and bulk preview UI
│   ├── features/study-workspace/       # Study rendering for canonical rich HTML
│   ├── services/                       # Bulk endpoint client + DTO mapping
│   ├── store/                          # Last-mode persistence + modal state
│   └── types.ts                        # Canonical card payload types
└── src/test/                           # Parser/modal behavior/component tests
```

**Structure Decision**: Use the existing web application split (`backend/` + `frontend/`) and implement this feature through additive schema/API/UI changes without introducing new top-level modules.

## Complexity Tracking

No constitution exceptions or complexity waivers are required for this plan.
