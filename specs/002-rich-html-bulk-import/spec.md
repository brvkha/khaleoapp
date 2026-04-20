# Feature Specification: Rich HTML Card Model + Bulk Card Import in Add Card Modal

**Feature Branch**: `002-rich-html-bulk-import`  
**Created**: 2026-04-17  
**Status**: Draft  
**Input**: User description: "Create a new feature specification (non-code) for khaleoapp that formalizes approved requirements for rich HTML card content and bulk import in Add Card modal."

## Clarifications

### Session 2026-04-17

- Q: Which audio tags/pattern are allowed for canonical rich content? -> A: Allow `audio` and `source` tags; support `<audio controls><source ...></audio>` for in-card playback; sanitize `src` attributes safely.
- Q: What is canonical update behavior for single-card updates? -> A: Phase 1 uses full update via PUT only; payload always includes both `frontContent` and `backContent`; PATCH is out of scope.
- Q: What standardized row error shape should bulk API use? -> A: Row errors use `{ code, message }` with line reference included in payload shape and/or message.
- Q: Should bulk row `error.code` be fixed global enum in phase 1? -> A: Yes; use a fixed global enum for phase 1 row errors.
- Q: What HTTP status should bulk endpoint return for full and partial success? -> A: Return HTTP 200 for both; use `failedCount` and `errors` to indicate partial success.
- Q: What is sanitizer behavior for disallowed markup in phase 1? -> A: Use STRIP policy; remove disallowed tags/attributes and preserve valid content, without rejecting a whole row solely for removable unsafe markup.
- Q: Which HTML attributes are allowed in phase 1? -> A: Allow `class` plus basic safe attributes only; inline `style` is disallowed.
- Q: What allowlist applies to audio/media URL domains in phase 1? -> A: Reuse the existing image URL allowlist for media URLs.
- Q: Is idempotency guaranteed for bulk retry in phase 1? -> A: No; duplicate creation on retry is accepted in phase 1.
- Q: How is the 500-card limit enforced? -> A: Enforce by JSON `cards` array length per request; if length is greater than 500, reject that request at request level with HTTP 400.
- Q: How are imports larger than 500 total rows handled in phase 1? -> A: Frontend parses all rows, chunks into requests of at most 500 cards, submits chunks sequentially, and reports cumulative progress.
- Q: How should bulk error line numbers be mapped? -> A: Map to original textarea line numbers, including blank lines.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create and study rich cards (Priority: P1)

As a deck owner, I can create and edit cards with rich HTML content on both front and back sides so study sessions display formatted text and media correctly.

**Why this priority**: This is the core model change and unlocks all downstream behavior (rendering, moderation, search, and imports).

**Independent Test**: Can be fully tested by creating and updating cards using only `frontContent` and `backContent`, then running a study session to confirm formatted content renders safely.

**Acceptance Scenarios**:

1. **Given** a user submits valid rich HTML using allowed tags, **When** a card is created or updated, **Then** the system stores sanitized HTML in `frontContent` and `backContent` and returns those fields in API responses.
2. **Given** a user submits dangerous HTML (for example `script` or `iframe` tags), **When** the card is created or updated, **Then** dangerous content is removed or rejected according to validation rules and never rendered as executable content.
3. **Given** a deck has migrated legacy cards, **When** the user opens a study session, **Then** the migrated rich content is displayed correctly and safely.

---

### User Story 2 - Import many cards quickly from spreadsheet text (Priority: P1)

As a deck owner, I can paste multiline tab- or comma-separated rows into Bulk import and preview parsed cards before submission so I can add many cards quickly.

**Why this priority**: Bulk authoring is a major workflow acceleration and the main new modal behavior.

**Independent Test**: Can be fully tested by pasting sample data from Google Sheets/Excel, validating preview parsing rules, submitting, and verifying partial success behavior.

**Acceptance Scenarios**:

1. **Given** bulk text with valid and invalid rows, **When** the user submits bulk import, **Then** valid rows are committed, invalid rows are skipped, and each chunk response reports `successCount`, `failedCount`, and line-level errors.
2. **Given** bulk text with more than 500 candidate cards (for example 3000), **When** the user submits, **Then** the frontend parses all rows first, splits cards into chunks of up to 500, and submits those chunks sequentially until complete or until a chunk request fails.
3. **Given** sequential chunk submission is in progress, **When** each chunk completes, **Then** the UI shows cumulative progress in the form `saved X/Y` based on successful row commits across completed chunks.
4. **Given** a chunk request fails due to network/server timeout, **When** the failure occurs, **Then** subsequent chunks are not sent, previously successful chunks remain committed, and the UI shows the failed chunk line range for retry.
5. **Given** a completed chunk has row-level failures, **When** the API returns HTTP 200 partial success for that chunk, **Then** the modal remains open and line-level errors are highlighted using original textarea line numbers.

---

### User Story 3 - Moderate and find cards after model change (Priority: P2)

As an admin or content manager, I can moderate and edit cards with a WYSIWYG editor and still find cards through search based on plain text content.

**Why this priority**: Keeps governance and discoverability functional after migration to HTML storage.

**Independent Test**: Can be tested by editing a card in moderation UI, saving rich content, and confirming search results still match plain-text queries derived from HTML.

**Acceptance Scenarios**:

1. **Given** a card includes HTML formatting and images, **When** admin edits it in moderation, **Then** the editor supports rich content without requiring Markdown.
2. **Given** a search query that matches visible card text, **When** search is executed, **Then** matching cards are found even if source content includes HTML tags.

---

### Edge Cases

- Empty lines and all-whitespace lines in bulk input are ignored and do not create rows.
- A row with only one column or empty front column is invalid and must be skipped with line-level error reporting.
- Rows with empty intermediate columns in back-side data preserve those empty positions when joining remaining columns by newline.
- Unicode input (emoji, Japanese, Korean, mixed scripts) remains intact through parse, sanitize, save, and render.
- External image and media URLs outside the existing allowlist are rejected and reported as validation failures.
- Preview only renders up to 100 parsed rows while still reporting total parsed count (for example, `showing 100/243`).
- Bulk row error line numbers map to original textarea line numbers, including blank lines.
- Imports with more than 500 total candidate rows are processed as multiple sequential requests of up to 500 cards each.
- If any chunk request fails at request level (for example network error, timeout, or server error), remaining chunks are not submitted and the failed chunk line range is shown for retry.
- If a client defect submits a chunk with more than 500 cards, backend rejects that chunk with HTTP 400 while preserving cards already committed by earlier successful chunks.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001 (Canonical Card Model)**: System MUST treat `frontContent` and `backContent` as the only canonical API/UI card content fields.
- **FR-002 (Storage Format)**: System MUST store canonical card content as sanitized HTML.
- **FR-003 (Allowed Tags)**: System MUST allow only this HTML tag whitelist in canonical content: `p`, `br`, `strong`, `em`, `u`, `img`, `audio`, `source`, `ul`, `ol`, `li`, `span`, `div`.
- **FR-004 (Sanitization STRIP Policy)**: System MUST strip disallowed tags/attributes (including dangerous tags such as `script` and `iframe`) and preserve remaining valid content; a whole card/row MUST NOT be rejected solely because removable unsafe markup was stripped.
- **FR-005 (Legacy Field Handling)**: System MUST keep legacy DB fields temporarily for backup compatibility, while API and UI exclusively read/write canonical fields.
- **FR-006 (Legacy Migration Rule - Front)**: Migration MUST transform legacy fields into canonical front content using: `frontContent = frontText + <br> + media`, where media is rendered as `<img>` or `<audio>` depending on source media type.
- **FR-007 (Legacy Migration Rule - Back)**: Migration MUST transform legacy fields into canonical back content using: `backContent = backText + <br> + examples + <br> + imageUrl`.
- **FR-008 (Bulk Parsing Input Scope)**: Bulk import parser MUST treat every non-empty line as a candidate card row and MUST NOT use header-row logic.
- **FR-009 (Line Splitting)**: Bulk parser MUST normalize CRLF to LF before splitting rows by newline.
- **FR-010 (Separator Selection)**: Bulk parser MUST support separator selection with tab as default and comma as optional.
- **FR-011 (Column Trimming)**: Bulk parser MUST trim whitespace around each parsed column value.
- **FR-012 (Front Mapping)**: Bulk parser MUST map the first column to front content input.
- **FR-013 (Back Mapping)**: Bulk parser MUST map back content input by joining all remaining columns (index >= 1) with newline characters.
- **FR-014 (Empty Intermediate Columns)**: Bulk parser MUST preserve empty intermediate columns while joining remaining columns.
- **FR-015 (Invalid Row Rules)**: A candidate row MUST be marked invalid if it has only one column or if trimmed front value is empty; invalid rows MUST be skipped and reported.
- **FR-016 (CSV Phase 1)**: Comma mode in phase 1 MUST use simple split parsing only and MUST NOT support quoted CSV escaping.
- **FR-017 (Preview Limit)**: Bulk preview MUST render at most 100 parsed rows and MUST display an indicator with shown versus total parsed rows.
- **FR-018 (Modal Modes)**: Add Card modal MUST provide two tabs: Single card and Bulk import.
- **FR-019 (Remember Last Mode)**: System MUST remember the last used Add Card tab in local storage and restore it when reopening the modal.
- **FR-020 (Bulk Submit Full Success)**: If bulk submission succeeds with zero failures, modal MUST close, bulk textarea MUST clear, and success toast MUST display.
- **FR-021 (Bulk Submit Partial Success)**: If bulk submission has any row failures, modal MUST remain open and MUST show summary plus line-level errors highlighted in preview.
- **FR-022 (Preview Actions Phase 1)**: Bulk preview MUST NOT include per-row delete actions in phase 1.
- **FR-023 (Bulk Endpoint)**: System MUST provide `POST /api/v1/decks/{deckId}/cards/bulk` for bulk creation.
- **FR-024 (Bulk Request Limit)**: Bulk endpoint MUST enforce a maximum of 500 cards by JSON `cards` array length; if `cards.length > 500`, that request MUST be rejected with HTTP 400 and no row processing.
- **FR-025 (Partial Commit Behavior)**: Bulk endpoint MUST allow partial commit and MUST NOT enforce all-or-nothing rollback semantics.
- **FR-026 (Bulk Response Contract)**: Bulk endpoint success response (full or partial) MUST return HTTP 200 and include `successCount`, `failedCount`, and `errors` entries.
- **FR-027 (Created IDs Omission)**: Bulk endpoint MUST NOT require returning created card IDs in phase 1.
- **FR-028 (Searchable Plain Text)**: Search MUST operate on plain text derived from HTML-stripped content using a stored searchable field suitable for SQL `LIKE` matching.
- **FR-029 (Study Rendering)**: Study session MUST render rich HTML card content in this release.
- **FR-030 (Moderation Upgrade)**: Admin card moderation/editing MUST use a lightweight WYSIWYG editor with canonical rich content fields in this release.
- **FR-031 (Editor Input Mode)**: Markdown authoring MUST be out of scope for this feature; WYSIWYG-only authoring is required for this release.
- **FR-032 (Audio Playback Pattern)**: Canonical content MUST support in-card audio playback using `<audio controls><source ...></audio>` with sanitizer-enforced safe `src` handling.
- **FR-033 (Canonical Update Semantics)**: Single-card update in phase 1 MUST use full replacement semantics via `PUT`; both `frontContent` and `backContent` are required on every update request.
- **FR-034 (No PATCH in Phase 1)**: `PATCH` semantics for canonical card updates MUST be out of scope for phase 1.
- **FR-035 (Standardized Bulk Row Error Object)**: Each bulk row error MUST expose standardized fields `{ line, code, message }` where `code` is from fixed phase 1 global enum: `ROW_TOO_FEW_COLUMNS`, `FRONT_REQUIRED`, `BACK_REQUIRED`, `MEDIA_DOMAIN_NOT_ALLOWED`, `HTML_UNRECOVERABLE`.
- **FR-036 (Allowed Attributes Policy)**: Sanitization MUST allow only `class` and basic safe attributes required by allowed tags (for example `src`, `alt`, `controls`, `type`); inline `style` MUST be disallowed.
- **FR-037 (Media URL Domain Policy)**: URL domain validation for `img`, `audio`, and `source` MUST use the same existing image allowlist in phase 1.
- **FR-038 (Bulk Line Number Mapping)**: Bulk `line` values sent and returned by API MUST map to original textarea line numbers including blank lines.
- **FR-039 (Bulk Retry Idempotency)**: Phase 1 bulk import MUST NOT guarantee idempotency; retrying the same request may create duplicates.
- **FR-040 (Frontend Full Parse Before Submit)**: Frontend MUST parse and validate all non-empty input lines into candidate rows before initiating bulk API submission.
- **FR-041 (Frontend Chunking Rule)**: When total candidate rows exceed 500, frontend MUST split payloads into chunks where each request contains at most 500 cards.
- **FR-042 (Sequential Chunk Submission)**: Frontend MUST submit chunk requests sequentially (next chunk starts only after the prior chunk request completes).
- **FR-043 (Cumulative Progress Feedback)**: During chunked submission, UI MUST display cumulative progress as `saved X/Y`, where `Y` is total candidate rows submitted for import and `X` is cumulative successful saves from completed chunks.
- **FR-044 (Chunk Failure Halt Behavior)**: If a chunk request fails at request level (network error, timeout, or non-2xx server response), frontend MUST stop submitting subsequent chunks.
- **FR-045 (Chunk Failure Recovery Context)**: On chunk request failure, UI MUST preserve prior successful chunk results and MUST show the failed chunk's original line range so user can retry from that chunk.
- **FR-046 (Per-Request Guard Preservation)**: Backend request-level max-size guard of 500 cards per request MUST remain enforced even when frontend orchestration supports imports larger than 500 total rows.

### Non-Functional and Security Requirements

- **NFR-001 (Dual Sanitization)**: HTML sanitization MUST be applied server-side before database writes and frontend-side before rendering.
- **NFR-002 (Media Domain Controls)**: Existing external image allowlist behavior MUST remain enforced and reused for media URLs (`img`, `audio`, `source`); non-allowlisted domains MUST be rejected.
- **NFR-003 (Safety Invariance)**: No stored or rendered card content may execute active script behavior in supported user flows.
- **NFR-004 (Bulk Preview Performance)**: Bulk preview rendering MUST use lightweight text rendering behavior (`white-space: pre-wrap`) and MUST NOT instantiate embedded rich-text editors per preview row.
- **NFR-005 (User Feedback Clarity)**: Bulk validation feedback MUST identify row line number and reason in user-visible form.
- **NFR-006 (Internationalization Robustness)**: Unicode text, including emoji and CJK content, MUST persist without corruption through import, storage, and rendering.
- **NFR-007 (URL Attribute Sanitization)**: Sanitization MUST validate URL-bearing attributes (including `src` on `img`, `audio`, and `source`) against allowed protocols/domains and strip unsafe values unless content becomes unrecoverable.
- **NFR-008 (Attribute Safety Controls)**: Sanitization MUST disallow inline `style` attributes and allow only `class` plus basic safe attributes.

### API Contract

#### 1) Card Create/Update Contract (Canonical Model)

- **Applies to**: existing single-card create API and single-card full update API.
- **Update method semantics (phase 1)**:
  - update is canonical full replacement via `PUT`.
  - update payload MUST include both `frontContent` and `backContent` on every request.
  - `PATCH` partial-update semantics are out of scope in phase 1.
- **Canonical payload fields**:
  - `frontContent` (string, required, sanitized HTML)
  - `backContent` (string, required, sanitized HTML)
- **Legacy payload fields**: `frontText`, `backText`, `examples`, `frontMediaUrl`, `imageUrl` are not part of the canonical API contract for this feature.
- **Validation expectations**:
  - Strip disallowed tags/attributes using phase 1 STRIP policy.
  - Allow only `class` and basic safe attributes; disallow inline `style`.
  - Sanitize URL-bearing attributes including `src` on media elements.
  - Enforce the same external allowlist for image and media URL domains.
  - Reject missing required canonical fields.

#### 2) New Bulk Create Endpoint

- **Method/Path**: `POST /api/v1/decks/{deckId}/cards/bulk`
- **Purpose**: create multiple cards from parsed rows in one request with partial success allowed.
- **Request body (phase 1 contract)**:

```json
{
  "cards": [
    {
      "line": 1,
      "frontContent": "<p>Front side</p>",
      "backContent": "<p>Back side</p>"
    }
  ]
}
```

- **Request rules**:
  - `cards` is required and contains 1 to 500 items per request.
  - each item requires `line`, `frontContent`, `backContent`.
  - `line` is the original textarea line number for error reporting, including blank lines.
  - `frontContent` and `backContent` use canonical HTML model and sanitization rules.

- **Success response (HTTP 200 for full success or partial success)**:

```json
{
  "successCount": 120,
  "failedCount": 3,
  "errors": [
    { "line": 7, "code": "FRONT_REQUIRED", "message": "Line 7: Front content is empty" },
    { "line": 18, "code": "MEDIA_DOMAIN_NOT_ALLOWED", "message": "Line 18: Media domain is not allowed" },
    { "line": 64, "code": "HTML_UNRECOVERABLE", "message": "Line 64: Content is unrecoverable after sanitization" }
  ]
}
```

- **Error behavior**:
  - Validation failures at row level are returned in `errors` with standardized `{ line, code, message }` where `code` is from the fixed phase 1 global enum.
  - Request-level violations (for example `cards.length > 500` in one request, malformed JSON, missing `cards`) return HTTP 400 and no processing for that request.
  - No idempotency guarantee is provided in phase 1; retried requests may create duplicate cards.
  - Created card IDs are omitted in phase 1.

- **Frontend orchestration for large imports (phase 1)**:
  - Frontend parses all candidate rows first, then constructs request chunks of up to 500 cards.
  - Frontend submits chunks sequentially and updates cumulative `saved X/Y` progress after each completed chunk.
  - A chunk response with HTTP 200 is treated as completed (full or partial success) using returned `successCount` and `failedCount`.
  - If a chunk request fails at request level (network/server timeout/non-2xx), frontend stops further chunk submission, preserves prior successful chunk commits, and displays the failed chunk line range for retry.

### Validation Matrix

| Operation | Field | Rule | Invalid Condition | Expected Outcome |
|-----------|-------|------|-------------------|------------------|
| Single Create | `frontContent` | Required, sanitized HTML, allowed tags/attributes only | Missing, blank after trim, unrecoverable after sanitization | Reject request with validation error |
| Single Create | `backContent` | Required, sanitized HTML, allowed tags only | Missing or blank after trim | Reject request with validation error |
| Single Create | Embedded media URL | Must pass existing allowlist shared by image/audio/source | Domain not in allowlist | Reject request with validation error |
| Single Update | `frontContent` | Required on update payload for canonical model | Missing field or blank content | Reject update |
| Single Update | `backContent` | Required on update payload for canonical model | Missing field or blank content | Reject update |
| Single Update | Method semantics | Full replacement via `PUT` with both canonical fields | PATCH-like partial payload or missing one canonical field | Reject update |
| Single Update | HTML safety | STRIP disallowed tags/attributes and disallow inline `style` | Contains unsafe tags/attributes | Strip unsafe markup; reject only if required content becomes invalid |
| Bulk Row Parse | Column count | Minimum 2 columns | Only one column after split | Skip row and report `{line, code, message}` |
| Bulk Row Parse | Front value | First column required after trim | Empty/whitespace front | Skip row and report `{line, code, message}` |
| Bulk Row Parse | Back composition | Join columns index >=1 by `\n` | Empty intermediate columns dropped | Treat as parser defect; must preserve empties |
| Bulk Submit | Request size | Max 500 cards by `cards` array length per request | `cards.length > 500` in a submitted chunk | Reject that request with HTTP 400; no processing for that request |
| Bulk Submit | Success status | HTTP 200 for full and partial success | Non-200 success response for partial | Contract violation in tests |
| Bulk Submit | Row content | Canonical validation per row | Any row fails sanitization/allowlist rules | Partial commit allowed; failed row reported |
| Bulk Submit | Line mapping | `line` maps to original textarea line index including blanks | Re-numbered after blank-line removal | Contract violation in tests |
| Bulk Submit | Idempotency | No phase 1 idempotency guarantee | Duplicate rows created on retry | Accepted phase 1 behavior |
| Bulk Submit | Response integrity | Count consistency | `successCount + failedCount` not equal input count | Reject as contract violation in tests |
| Frontend Orchestration | Pre-submit parse | Parse all non-empty rows before first request | Submit starts before full parse completes | Contract violation in tests |
| Frontend Orchestration | Chunking | Split total rows into chunks of max 500 cards | Any submitted chunk contains >500 cards | Request rejected by backend guard; frontend must stop and surface failed range |
| Frontend Orchestration | Submission order | Sequential chunk submission only | Parallel chunk dispatch | Contract violation in tests |
| Frontend Orchestration | Progress | Show cumulative `saved X/Y` updates after each chunk | Missing or non-cumulative progress state | Contract violation in tests |
| Frontend Orchestration | Failure handling | Stop on failed chunk request and preserve prior successes | Continue sending later chunks after failed request | Contract violation in tests |
| Frontend Orchestration | Retry scope | Show failed chunk original line range for retry | Retry context omits failed line range | Contract violation in tests |

### Migration and Backward Compatibility Strategy

- Preserve legacy DB fields during phase 1 for backup compatibility and rollback confidence.
- Run one-time migration to populate canonical HTML fields from legacy content using approved transformation rules.
- After migration, API and UI workflows use canonical fields only; legacy fields are not exposed in normal product flows.
- Maintain read consistency by ensuring every card used by study/search/moderation has canonical content populated.
- Search indexing/backfill must derive plain-text searchable content from canonical HTML and keep it in sync for SQL `LIKE` queries.

### WYSIWYG Editor Decision Record

- **Decision**: use a lightweight WYSIWYG editor and explicitly avoid Markdown authoring in phase 1.
- **Candidates**: TipTap, Quill.
- **Selection criteria**:
  - supports basic formatting and list/image capabilities required by allowed tags.
  - predictable HTML output that maps to sanitizer/allowlist constraints.
  - low integration complexity in existing Add Card and moderation screens.
  - acceptable typing and paste performance for multilingual content.
- **Decision checkpoint**: final library choice is completed during implementation planning, using the above criteria with a short spike if needed.

### Out of Scope (Phase 1)

- Quoted/escaped RFC-compliant CSV parsing.
- Header-row detection or mapping UI.
- Per-row delete controls inside preview.
- Returning created card IDs from bulk API response.
- Markdown editing mode.
- Rich editor instances embedded inside bulk preview rows.

### Key Entities *(include if feature involves data)*

- **CardContent**: Canonical content pair (`frontContent`, `backContent`) stored as sanitized HTML and used by API/UI.
- **LegacyCardFields**: Existing backup fields retained temporarily for migration safety; not canonical for new workflows.
- **BulkImportRow**: Parsed row object with source `line`, parsed front value, parsed back value, and validation status.
- **BulkImportResult**: Request outcome summary containing `successCount`, `failedCount`, and row-level `errors` with standardized `{ code, message }` plus line reference.
- **SearchTextIndex**: Plain-text representation derived from card HTML for SQL `LIKE` search behavior.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of newly created or updated cards in this release use canonical `frontContent` and `backContent` in API/UI flows.
- **SC-002**: During bulk import, users can submit datasets larger than 500 total candidate rows (for example 3000) in one import workflow via automatic client-side chunking into sequential requests of at most 500 cards each.
- **SC-003**: For mixed-validity chunked submissions, users can identify and correct failed rows from modal feedback (original line mapping preserved) without losing successful commits from completed chunks.
- **SC-004**: At least 95% of tested copy-paste imports from Google Sheets/Excel produce expected parsed row structures under phase 1 split rules.
- **SC-005**: Study and moderation flows render rich content without executing active scripts across acceptance test scenarios.
- **SC-006**: Search continues returning expected matches for card text after HTML migration via plain-text derived indexing.
- **SC-007**: During chunked import execution, users see accurate cumulative progress (`saved X/Y`) updated after each completed chunk.
- **SC-008**: If any chunk request fails at request level, the workflow halts within that import run, completed chunks remain saved, and users are shown the failed chunk line range for targeted retry.

## Assumptions

- Existing single-card create/update APIs already exist and can be extended to canonical fields without route redesign.
- Sanitizer policy can both remove unsafe markup and return validation errors where policy requires hard rejection.
- Bulk import UI sends parsed rows (with source line numbers) to the bulk endpoint rather than raw textarea text.
- Frontend retry action after a failed chunk is user-initiated and scoped from the failed chunk range onward; already successful chunks are not rolled back.
- Audio media from legacy `frontMediaUrl` is represented in canonical HTML using an allowed-safe rendering pattern compatible with sanitization policy.
- Current local storage use in frontend is acceptable for remembering last used Add Card tab.

## Open Questions

- None currently. Prior phase 1 decisions are resolved in Clarifications and reflected in FR/NFR/API contract.












