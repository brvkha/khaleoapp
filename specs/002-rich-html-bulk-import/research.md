# Research: Rich HTML Card Model + Bulk Import

## 1) Backend HTML sanitization policy and implementation
- Decision: Use a backend sanitizer with explicit allowlist configuration (Jsoup Safelist-style policy) to enforce allowed tags/attributes and STRIP behavior before persistence.
- Rationale: The feature requires removing disallowed markup without rejecting recoverable content, plus strict control of URL-bearing attributes (`src`) and inline style rejection.
- Alternatives considered: Regex-based HTML filtering (too fragile for nested/encoded HTML), reject-on-any-disallowed-tag policy (conflicts with FR-004 STRIP requirement).

## 2) Frontend render-time sanitization and parity
- Decision: Sanitize again on the frontend using a strict DOM sanitizer profile aligned to backend allowlist and URL rules.
- Rationale: NFR-001 requires dual sanitization, and frontend enforcement protects against regressions or stale data that bypasses backend write paths.
- Alternatives considered: Backend-only sanitization (violates NFR-001), render raw trusted HTML (unacceptable XSS risk).

## 3) Canonical content model and migration shape
- Decision: Introduce canonical `front_content`/`back_content` HTML fields while retaining legacy text/media fields in phase 1; backfill canonical from legacy using the approved transformation rules.
- Rationale: This preserves rollback safety while shifting API/UI reads and writes to canonical content immediately.
- Alternatives considered: Hard cutover removing legacy columns in same release (higher rollback risk), keeping legacy as primary and deriving canonical on read (breaks FR-001 canonical model).

## 4) Bulk parser and line mapping strategy
- Decision: Parse client-side textarea input with CRLF->LF normalization, split by original lines, ignore blank/whitespace-only lines as candidates, preserve original line numbers, and preserve empty intermediate back-side columns when joining with `\n`.
- Rationale: Matches FR-009/014/038 and supports precise user remediation in modal preview.
- Alternatives considered: Server-side parsing of raw text (less responsive UX and weaker preview), CSV library with quote support in phase 1 (out of scope by FR-016).

## 5) Bulk API transaction and error contract
- Decision: Implement row-level validation + create loop with partial commit semantics; return HTTP 200 for full and partial success with `{ successCount, failedCount, errors[] }`, and enforce request-level 400 for invalid envelope (including `cards.length > 500`).
- Rationale: Satisfies FR-024/025/026 and keeps remediation workflow in one modal session.
- Alternatives considered: All-or-nothing transaction rollback (conflicts with FR-025), HTTP 207 for partial success (conflicts with clarified status requirement).

## 6) Search behavior under HTML storage
- Decision: Persist a derived plain-text search field generated from stripped canonical HTML (for front and back combined) and keep it updated on create/update/bulk import.
- Rationale: FR-028 requires SQL LIKE-friendly search behavior after migration to HTML source content.
- Alternatives considered: Stripping HTML at query time (higher query cost and poorer indexability), full-text engine introduction in phase 1 (scope increase).

## 7) WYSIWYG editor selection for phase 1
- Decision: Use TipTap as the lightweight WYSIWYG editor for Add Card and moderation rich-content authoring.
- Rationale: Good control over generated HTML schema, predictable extension model for allowed tags, and practical integration path with React stack.
- Alternatives considered: Quill (strong baseline but less flexible for strict schema normalization), Markdown mode (explicitly out of scope).

