# Data Model: Rich HTML Card Model + Bulk Import

## Entities

### 1) Card (canonicalized)
- Description: Core study item with canonical rich HTML content for both sides.
- Fields (new/changed):
  - `id` (UUID, PK)
  - `deck_id` (UUID, FK -> Deck, required)
  - `front_content` (TEXT, required, sanitized HTML)
  - `back_content` (TEXT, required, sanitized HTML)
  - `search_text` (TEXT, required, plain text derived from canonical HTML)
  - `version` (LONG, required, optimistic lock)
  - Legacy retained fields for phase 1 compatibility/rollback: `front_text`, `back_text`, `front_media_url`, `back_media_url`, `image_url`, `examples_json`, etc.
- Validation rules:
  - Both canonical fields required on create and `PUT` update.
  - Allowed tags only: `p`, `br`, `strong`, `em`, `u`, `img`, `audio`, `source`, `ul`, `ol`, `li`, `span`, `div`.
  - Allowed attributes are restricted to `class` plus safe attributes required by allowed tags (e.g., `src`, `alt`, `controls`, `type`); inline `style` forbidden.
  - URL-bearing attributes must pass allowed protocol/domain policy (reuse image allowlist for media).

### 2) BulkImportCardInput (request DTO)
- Description: One candidate row payload sent by frontend parser.
- Fields:
  - `line` (INT, required, original textarea line number including blanks)
  - `frontContent` (STRING, required)
  - `backContent` (STRING, required)
- Validation rules:
  - Request-level list size: `1..500`; reject `>500` with HTTP 400 and no processing.
  - Row-level content follows canonical sanitizer and domain rules.

### 3) BulkImportError
- Description: Standardized row failure contract returned by bulk endpoint.
- Fields:
  - `line` (INT, required)
  - `code` (ENUM, required)
  - `message` (STRING, required)
- Global enum values (phase 1 fixed):
  - `ROW_TOO_FEW_COLUMNS`
  - `FRONT_REQUIRED`
  - `BACK_REQUIRED`
  - `MEDIA_DOMAIN_NOT_ALLOWED`
  - `HTML_UNRECOVERABLE`

### 4) BulkImportResult
- Description: Aggregated result of one bulk submit.
- Fields:
  - `successCount` (INT, required)
  - `failedCount` (INT, required)
  - `errors` (ARRAY<BulkImportError>, required; empty array allowed)
- Invariants:
  - `successCount + failedCount == input card count`
  - HTTP 200 used for both full and partial success

### 5) AddCardModalState (frontend)
- Description: UI state model for single vs bulk authoring flow.
- Fields:
  - `mode` (`single` | `bulk`)
  - `bulkRawText` (STRING)
  - `separator` (`tab` | `comma`)
  - `parsedRows` (ARRAY of preview rows)
  - `lastModeStorageKey` (STRING in localStorage)
- Rules:
  - Persist/restore last used mode.
  - Render max 100 preview rows while showing total count.

## Relationships
- Deck `1 -> N` Card.
- BulkImportCardInput `N -> 1` BulkImportResult (per request aggregation).
- BulkImportResult `1 -> N` BulkImportError.

## State transitions
- Single card create:
  - Draft input -> sanitize/validate -> persist canonical fields + search text -> return canonical response.
- Single card update (`PUT` only):
  - Existing card -> full canonical payload required -> sanitize/validate -> replace canonical fields + refresh search text.
- Bulk import row lifecycle:
  - Parsed row -> request DTO -> sanitize/validate ->
    - success branch: card persisted and counted in `successCount`
    - failure branch: error object appended with original `line` and enum `code`.
- Modal submission UX:
  - Full success (`failedCount=0`): close modal, clear textarea, show success toast.
  - Partial success (`failedCount>0`): keep modal open, show summary and line-level highlights.

