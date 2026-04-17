# Quickstart: Rich HTML Card Model + Bulk Import

## Goal
Implement canonical rich HTML card fields plus bulk import in Add Card modal with partial success handling.

## 1) Backend implementation outline
1. Add Flyway migration(s) for canonical fields and search index support in `backend/src/main/resources/db/migration/`.
   - Add `front_content`, `back_content`, and `search_text` to `cards`.
   - Keep legacy columns in place for phase 1.
2. Backfill canonical fields from legacy content per approved migration rules.
3. Update card entity/repository/service/controller to use canonical create/update contracts.
   - `PUT /api/v1/cards/{id}` requires both `frontContent` and `backContent`.
4. Add bulk endpoint `POST /api/v1/decks/{deckId}/cards/bulk`.
   - Enforce `cards.length <= 500` request-level guard.
   - Execute row-level validation/sanitization and partial commit behavior.
   - Return `{ successCount, failedCount, errors }` with standardized row errors.
5. Derive and persist `search_text` from canonical HTML on single and bulk writes.

## 2) Frontend implementation outline
1. Update Add Card modal with two tabs: `Single card` and `Bulk import`.
2. Persist last-selected tab in localStorage and restore on reopen.
3. Build bulk parser:
   - Normalize CRLF to LF.
   - Split by line and preserve original line numbers.
   - Tab default separator, comma optional.
   - First column -> front, remaining columns joined with `\n` -> back, preserving empty intermediate columns.
4. Build preview table/list with max 100 rendered rows and total parsed indicator.
5. Submit parsed rows to bulk endpoint and apply UX rules:
   - Full success: close and clear modal.
   - Partial success: keep modal open, highlight line-level errors.
6. Render canonical HTML safely in study and moderation surfaces with frontend sanitization.

## 3) Testing checklist
- Backend unit/integration tests:
  - sanitizer allowlist/strip behavior
  - media allowlist enforcement on `img/audio/source`
  - request-level 400 for `cards.length > 500`
  - partial success count/error invariants
  - line number passthrough and response mapping
  - canonical `PUT` full-replacement validation
- Frontend unit/component tests:
  - parser behavior for tab/comma modes and blank-line mapping
  - preview cap at 100 rows with total indicator
  - modal mode persistence in localStorage
  - partial-success error highlight behavior
- Contract tests:
  - bulk response schema and fixed enum codes
  - create/update canonical payload requirements

## 4) Suggested local verification commands
```powershell
Set-Location C:\Workspace\FPT\khaleoapp\backend
.\mvnw.cmd test
```

```powershell
Set-Location C:\Workspace\FPT\khaleoapp\frontend
npm test
npm run test:e2e:list
```

