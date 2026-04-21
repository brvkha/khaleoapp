# Research: Listening Dictation + Admin CMS (Phase 1)

## 1) Hierarchical CMS domain model for listening content
- Decision: Use a strict parent-child hierarchy `Topic -> Exercise -> Lesson -> Sentence` with scoped slug uniqueness (`exercise.slug` unique per `topic_id`, `lesson.slug` unique per `exercise_id`).
- Rationale: Matches the approved drill-down authoring flow and avoids global slug collisions such as repeated `part-1` across different exercise sets.
- Alternatives considered: Global unique slugs (harder authoring and noisy URLs), no slug constraints (ambiguous routing and unstable links).

## 2) Sentence ordering and admin drag-drop persistence
- Decision: Persist sentence order using deterministic `order_index` and a reorder endpoint that accepts ordered sentence IDs for one lesson.
- Rationale: Keeps order updates atomic at lesson scope and aligns with learner playback/transcript sequencing.
- Alternatives considered: Fractional ranking keys (adds complexity in phase 1), client-only reorder without persistence (breaks learner consistency).

## 3) JSON bulk import contract for sentence authoring
- Decision: Implement `import-json` as partial-success processing with response `{ successCount, failedCount, errors[] }`, where each error includes row index and validation message.
- Rationale: Admin can import large scraped datasets without losing valid rows, then quickly fix only failing rows.
- Alternatives considered: All-or-nothing import rollback (slower correction loop), CSV-only import in phase 1 (conflicts with approved JSON import requirement).

## 4) Hybrid media playback strategy (sentence clip vs shared lesson media)
- Decision: Support both media modes: (a) sentence-level media file, (b) lesson-level shared media sliced by `start_time/end_time`; validate only `start_time >= 0` and `end_time > start_time` in phase 1.
- Rationale: Supports mixed content sources while keeping backend validation lightweight and avoiding S3 duration probing in write path.
- Alternatives considered: Single media mode only (insufficient for mixed datasets), server duration validation on save (higher latency/complexity).

## 5) Protected media delivery using presigned URLs + object URLs
- Decision: Backend issues short-lived S3 presigned URLs; frontend fetches bytes as blob/arraybuffer and renders `blob:` object URLs in `<audio>`.
- Rationale: Preserves scalable S3 delivery while reducing direct exposure of origin URLs in DOM/player controls.
- Alternatives considered: Direct public URLs (weak content protection), full backend streaming proxy for all media (higher backend load in phase 1).

## 6) Dictation matching algorithm and UX masking
- Decision: Use exact full-string match after approved normalization pipeline and alias canonicalization; no fuzzy tolerance. UI reveals correct words left-to-right and masks from first mismatch onward with `***`.
- Rationale: Preserves strict dictation pedagogy and deterministic grading while giving immediate directional feedback.
- Alternatives considered: Levenshtein/fuzzy scoring (drifts from strict method), token-percentage scoring (ambiguous pass criteria).

## 7) Dictionary integration via backend proxy
- Decision: Add backend dictionary proxy endpoint for Cambridge lookup with server-side key handling and controlled failure payload for graceful frontend fallback.
- Rationale: Prevents API key leakage/quota abuse and keeps learner session stable during provider outages.
- Alternatives considered: Frontend direct provider call (key exposure/CORS risk), postponing dictionary integration (misses approved phase 1 requirement).

## 8) Settings persistence and keyboard control model
- Decision: Persist listening settings in localStorage (`replayKey`, `playPauseKey`, `autoPlayOnNext`, `autoReplayCount`, `replayIntervalSeconds`, transcript auto-scroll/loop toggles).
- Rationale: Preserves learner preferences across sessions with minimal backend footprint in phase 1.
- Alternatives considered: Server-side settings first (extra API/schema scope), hard-coded controls with no settings (reduced UX flexibility).

