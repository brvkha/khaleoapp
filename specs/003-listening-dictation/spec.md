# Feature Specification: Listening Dictation + Admin CMS (Phase 1)

**Feature Branch**: `003-listening-dictation`  
**Created**: 2026-04-21  
**Status**: Finalized  
**Input**: User description: "Create a new feature specification for khaleoapp for BIG FEATURE 'Listening Dictation' using the user's finalized decisions."

## Clarifications

### Session 2026-04-21

- Include both learner-facing dictation workspace and full Admin CMS in phase 1.
- Admin hierarchy is Topic -> Exercise -> Lesson -> Sentence.
- Admin CMS provides 4 top-level management tabs for Topics, Exercises, Lessons, and Sentences (flat search/CRUD for each entity).
- Admin users can use the standard Learner Workspace for dictation practice.
- Admin sentence management includes drag-drop reorder and JSON bulk sentence import.
- Media delivery uses short-lived pre-signed URLs; frontend downloads media as blob/arraybuffer and plays via object URL.
- Dictation correctness uses exact full-sentence equality after normalization plus alias mapping only (no fuzzy matching).
- Sentence completion is recorded only when learner answer is correct on Check, or learner uses Skip.
- Dictionary lookup uses Cambridge via backend proxy endpoint; provider key remains server-side; frontend shows graceful fallback when provider fails.
- Slug uniqueness is parent-scoped (exercise slug unique within topic; lesson slug unique within exercise).
- Sentence translation is nullable; learner UI shows fallback text or hides translation section when absent.
- Shared lesson media timestamp rules: `start_time >= 0` and `end_time > start_time`; phase 1 does not validate against real media duration.
- Attempt history is out of scope in phase 1 (no per-try persistence).
- Learner setting "Auto Play on Next" is user-selectable behavior.

### Session 2026-04-28

- Q: Should Admin CMS and learner workspace use one route or separate routes? → A: Separate routes: `/admin/listening` for CMS and `/listening` for learner workspace.
- Q: What admin navigation model is final? → A: Four flat CRUD/search tabs (Topics, Exercises, Lessons, Sentences) using shared base components plus entity-specific tab behavior.
- Q: How should sentence deletion affect learner progress rows? → A: Deleting a sentence cascades and removes related `user_sentence_progress` rows.
- Q: Where is sentence reorder available? → A: Reorder is available from both Lessons tab context and Sentences tab context.
- Q: Where is JSON bulk sentence import available? → A: Import is initiated from Lessons tab context.
- Q: What JSON import contract is required? → A: Payload is array-of-objects; response includes per-index validation errors and partial success counts.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Manage Listening Content in Admin CMS (Priority: P1)

As an admin/content manager, I can create and maintain listening curriculum content (topics, exercises, lessons, sentences) through 4 separate management tabs (Topics, Exercises, Lessons, Sentences) so learners can practice dictation with structured, publishable materials.

**Why this priority**: Without content authoring and maintenance, learner dictation cannot operate in production.

**Independent Test**: Can be tested independently by creating one topic, one exercise, one lesson, adding/reordering sentences, importing sentence JSON, and verifying data is retrievable by learners.

**Acceptance Scenarios**:

1. **Given** an admin is in CMS, **When** they navigate through Topics, Exercises, Lessons, or Sentences tabs, **Then** they can search, list, and perform CRUD for each entity type using shared base components and entity-specific tab behavior.
2. **Given** a lesson has multiple sentences, **When** admin reorders sentences from Lessons tab context or Sentences tab context and saves, **Then** learner delivery reflects the updated order.
3. **Given** admin uploads valid sentence JSON payload from Lessons tab context, **When** bulk import is submitted as an array of objects, **Then** valid rows are created/updated, invalid rows are returned with per-index validation errors, and response includes partial success counts.
4. **Given** an exercise slug already exists inside the same topic, **When** admin reuses that slug in that topic, **Then** the request is rejected as duplicate scope conflict.

---

### User Story 2 - Complete Dictation Practice in Learner Workspace (Priority: P1)

As a learner (including Admin users), I can listen to sentence audio and submit dictation answers so I can practice listening accuracy and track completion.

**Why this priority**: This is the core learner value of the Listening Dictation feature.

**Independent Test**: Can be tested independently by loading any lesson with audio, submitting at least one correct dictation answer, and verifying progress tracking.

**Acceptance Scenarios**:

1. **Given** a learner is in the workspace for a lesson with audio, **When** they play the audio and submit a correct dictation answer, **Then** the system marks the sentence as complete and progresses to the next sentence according to the "Auto Play on Next" setting.
2. **Given** an authenticated user with admin role opens `/listening`, **When** they start a lesson, **Then** they can perform learner dictation practice with the same completion and playback behaviors as non-admin learners.
3. **Given** a learner has not completed a sentence, **When** they return to the lesson, **Then** the sentence is in the same state as they left it, with audio replay and answer re-check available.
4. **Given** a learner selects a dictionary term lookup, **When** the dictionary provider is unavailable, **Then** the system shows a fallback message and allows continued practice without interruption.

---

### Functional Requirements

- **FR-001 (Phase 1 Scope)**: System MUST deliver both learner module and full Admin CMS for Listening Dictation in phase 1.
- **FR-002 (Route Separation)**: System MUST expose separate routes: `/admin/listening` for Admin CMS and `/listening` for learner workspace; users with admin role MUST be able to access learner workspace via `/listening`.
- **FR-003 (Admin UI Structure)**: Admin CMS MUST provide 4 top-level management tabs (Topics, Exercises, Lessons, Sentences) for flat search and CRUD operations, implemented with a shared tab base and entity-specific tab modules.
- **FR-004 (Admin Topic CRUD)**: System MUST provide search/create/read/update/delete operations for `topics`.
- **FR-005 (Admin Exercise CRUD)**: System MUST provide search/create/read/update/delete operations for `exercises`.
- **FR-006 (Admin Lesson CRUD)**: System MUST provide search/create/read/update/delete operations for `lessons`.
- **FR-007 (Admin Sentence CRUD)**: System MUST provide search/create/read/update/delete operations for `sentences`.
- **FR-008 (Exercise Slug Scope Rule)**: System MUST enforce exercise slug uniqueness within the same topic (duplicate allowed across different topics).
- **FR-009 (Lesson Slug Scope Rule)**: System MUST enforce lesson slug uniqueness within the same exercise (duplicate allowed across different exercises).
- **FR-010 (Sentence Reorder Entry Points)**: System MUST allow admin to reorder sentences within a lesson from both Lessons tab context and Sentences tab context, and persist deterministic sequence order.
- **FR-011 (Sentence JSON Bulk Import)**: System MUST support sentence JSON bulk import from Lessons tab context with an array-of-objects payload, per-index validation errors, and partial success reporting (`successCount`, `failedCount`).
- **FR-012 (Sentence Delete Cascade)**: System behavior MUST ensure that deleting a sentence removes related `user_sentence_progress` rows for that sentence; this defines expected runtime behavior, while FR-043 defines the mandatory data-model/schema constraint that enforces it.
- **FR-013 (Sentence Translation Optionality)**: System MUST allow `sentences.translation` to be null.
- **FR-014 (Translation Fallback Rendering)**: Learner UI MUST hide translation section or show fallback text when sentence translation is null.
- **FR-015 (Hybrid Media Strategy)**: System MUST support sentence media through either sentence-specific media reference or shared lesson media with sentence timestamps.
- **FR-016 (Shared Media Timestamp Validation)**: For shared lesson media mode, system MUST validate `start_time >= 0` and `end_time > start_time`.
- **FR-017 (No Duration Validation in Phase 1)**: System MUST NOT validate timestamps against actual media duration in phase 1, and phase 1 MUST NOT issue any backend or frontend media-duration validation call as part of save/import/playback flows.
- **FR-018 (Pre-Signed Media Access)**: System MUST deliver learner media through pre-signed URL exchange with a phase 1 TTL between 60 and 300 seconds (inclusive).
- **FR-019 (Frontend Playback Source Rule)**: Learner client MUST fetch media bytes (blob/arraybuffer) and use object URL playback source.
- **FR-020 (Dictation Answer Rule)**: System MUST evaluate Check correctness using exact full-string equality only after applying the normalization pipeline in **Dictation Normalization & Matching Rules**.
- **FR-021 (No Fuzzy Matching)**: System MUST NOT apply fuzzy, phonetic, edit-distance, token-level, or partial-match scoring in phase 1.
- **FR-022 (Normalization Source of Truth)**: Any dictation correctness logic change MUST update **Dictation Normalization & Matching Rules** first, and implementations/tests MUST follow that section.
- **FR-023 (Completion Tracking Rule)**: System MUST mark sentence completion only on (a) correct Check or (b) explicit Skip action.
- **FR-024 (No Attempt History in Phase 1)**: System MUST NOT persist per-try attempt history in phase 1.
- **FR-025 (User Sentence Progress Persistence)**: System MUST persist completion state in `user_sentence_progress` keyed by user and sentence.
- **FR-026 (Learner Workspace Components)**: Learner workspace MUST include dictation input/check flow, full transcript view, audio player controls, personal settings, and progress indicators.
- **FR-027 (Auto Play Setting)**: System MUST provide a user-selectable `Auto Play on Next` setting and apply it when advancing to next sentence.
- **FR-028 (Keyboard Shortcuts)**: Learner workspace MUST support default shortcuts: Enter=Check, Esc=Skip, Ctrl=Replay, backtick=Play/Pause.
- **FR-029 (Auto Replay Controls)**: Settings MUST support configurable auto replay count per sentence (None, 1, 2, Infinite) and replay interval enum values fixed for phase 1 as (0.5s, 1.0s, 1.5s).
- **FR-030 (Full Transcript Playback Controls)**: Full Transcript tab MUST support click-to-play by sentence, optional auto-scroll that keeps active sentence near the third visible line, and optional loop/repeat for full lesson playback.
- **FR-031 (Clickable Word Lookup)**: In answer-visible and transcript contexts, English words MUST be clickable to request dictionary lookup.
- **FR-032 (Dictionary Proxy)**: System MUST provide dictionary lookup via backend proxy to Cambridge provider with provider key hidden server-side.
- **FR-033 (Dictionary Fallback UX)**: If dictionary provider fails, learner UI MUST show fallback messaging in-context and keep dictation flow available without forced navigation/reload.
- **FR-034 (Learner Retrieval API)**: System MUST provide learner-facing retrieval APIs for topic/exercise/lesson/sentence structures and current learner progress.
- **FR-035 (Progress Update API)**: System MUST provide endpoint(s) to update completion state for correct-check and skip actions.
- **FR-036 (Pre-Signed URL Exchange API)**: System MUST provide endpoint(s) for issuing media access URLs for lesson or sentence media with TTL constrained to 60-300 seconds in phase 1.

### Data Model Requirements

- **FR-037 (`topics` table)**: `topics` MUST store identity, display metadata, slug, lifecycle status, and audit timestamps.
- **FR-038 (`exercises` table)**: `exercises` MUST store parent `topic_id`, identity, display metadata, slug, status, and audit timestamps.
- **FR-039 (`lessons` table)**: `lessons` MUST store parent `exercise_id`, identity, display metadata, slug, summary fields, optional shared media reference, status, and audit timestamps.
- **FR-040 (`sentences` table)**: `sentences` MUST store parent `lesson_id`, ordered position, canonical text, optional translation, optional aliases mapping, media mode fields (sentence media reference and/or shared-media timestamps), and audit timestamps.
- **FR-041 (`user_sentence_progress` table)**: `user_sentence_progress` MUST store `user_id`, `sentence_id`, completion flag/status, completion source (`correct_check` or `skip`), completion timestamp, and last-updated timestamp.
- **FR-042 (Progress Uniqueness)**: `user_sentence_progress` MUST enforce one progress row per `(user_id, sentence_id)`.
- **FR-043 (Sentence Delete Cascade Rule)**: Data model/schema MUST enforce referential delete cascade from `sentences.id` to `user_sentence_progress.sentence_id`; this schema constraint is the enforcement mechanism for the behavior required by FR-012.
- **FR-044 (Scoped Slug Constraints)**: Data model MUST enforce scoped uniqueness constraints for exercise and lesson slug rules described in FR-008 and FR-009.

### Security, Abuse Prevention, and Observability Requirements

- **FR-045 (Dictionary Provider Host Allowlist)**: Dictionary proxy outbound requests MUST be restricted to an explicit allowlist of approved dictionary provider hostnames; requests to non-allowlisted hosts MUST be blocked.
- **FR-046 (Dictionary Outbound Timeout Policy)**: Dictionary proxy outbound calls MUST enforce bounded timeout policy (connection and response) with fail-fast behavior and controlled timeout failure responses.
- **FR-047 (Dictionary Term Input Validation)**: Dictionary lookup term input MUST be sanitized and validated before proxying (including trimming, length bounds, and character validation); invalid input MUST be rejected with a controlled validation response.
- **FR-048 (Dictionary Response Size and Abuse Controls)**: Dictionary proxy MUST enforce response-size limits and abuse controls (including request throttling/rate limiting) to prevent oversized provider responses and repeated lookup abuse from degrading the listening experience.
- **FR-049 (Structured Logging for Critical Flows)**: System MUST emit structured logs for critical listening flows, including learner workspace retrieval, media access issuance, progress updates, dictionary lookup requests, and corresponding failures.
- **FR-050 (Correlation ID Propagation)**: System MUST accept or generate a correlation ID per request and propagate it through listening/media/dictionary processing paths, including structured logs and error responses for traceability.

### Performance Expectations

**Baseline assumptions for all p95 targets**:
- Measurements use production-like UAT load with authenticated requests, stable network conditions, and warm service/runtime state.
- Payload sizes represent normal phase 1 usage (typical lesson sizes and standard admin list queries).
- p95 is measured over at least 1,000 requests per endpoint family during the same test window.
- Targets in this section MUST comply with constitution Principle IV baseline for interactive backend APIs (`p95 <= 300 ms`).

- **PE-001 (Learner Workspace Fetch p95)**: `GET /api/v1/listening/lessons/{lessonId}/workspace` responses SHOULD complete within **300 ms p95**.
- **PE-002 (Media Access Issuance p95)**: `POST /api/v1/listening/media/access` responses SHOULD complete within **300 ms p95**.
- **PE-003 (Admin List Endpoints p95)**: Admin list/read endpoints for Topics, Exercises, Lessons, and Sentences SHOULD complete within **300 ms p95**.

### API Contract

#### 1) Admin CMS APIs

- `GET /api/v1/admin/listening/topics` (flat Topics tab list/search)
- `POST /api/v1/admin/listening/topics`
- `GET /api/v1/admin/listening/topics/{topicId}`
- `PUT /api/v1/admin/listening/topics/{topicId}`
- `DELETE /api/v1/admin/listening/topics/{topicId}`

- `GET /api/v1/admin/listening/exercises` (flat Exercises tab list/search)
- `POST /api/v1/admin/listening/exercises`
- `GET /api/v1/admin/listening/exercises/{exerciseId}`
- `PUT /api/v1/admin/listening/exercises/{exerciseId}`
- `DELETE /api/v1/admin/listening/exercises/{exerciseId}`

- `GET /api/v1/admin/listening/lessons` (flat Lessons tab list/search)
- `POST /api/v1/admin/listening/lessons`
- `GET /api/v1/admin/listening/lessons/{lessonId}`
- `PUT /api/v1/admin/listening/lessons/{lessonId}`
- `DELETE /api/v1/admin/listening/lessons/{lessonId}`

- `GET /api/v1/admin/listening/sentences` (flat Sentences tab list/search)
- `POST /api/v1/admin/listening/sentences`
- `GET /api/v1/admin/listening/sentences/{sentenceId}`
- `PUT /api/v1/admin/listening/sentences/{sentenceId}`
- `DELETE /api/v1/admin/listening/sentences/{sentenceId}`

- `GET /api/v1/admin/listening/topics/{topicId}/exercises` (contextual list under Topic)
- `POST /api/v1/admin/listening/topics/{topicId}/exercises` (contextual create under Topic)
- `GET /api/v1/admin/listening/exercises/{exerciseId}/lessons` (contextual list under Exercise)
- `POST /api/v1/admin/listening/exercises/{exerciseId}/lessons` (contextual create under Exercise)
- `GET /api/v1/admin/listening/lessons/{lessonId}/sentences` (contextual list under Lesson)
- `POST /api/v1/admin/listening/lessons/{lessonId}/sentences` (contextual create under Lesson)
- `PUT /api/v1/admin/listening/lessons/{lessonId}/sentences/reorder`
- `PUT /api/v1/admin/listening/sentences/reorder`
- `POST /api/v1/admin/listening/lessons/{lessonId}/sentences/import-json`

**Contract expectations**:
- Flat tab list/search contracts MUST be unambiguous and available for each top-level tab endpoint:
  - `GET /api/v1/admin/listening/topics`: supports search filter `q`.
  - `GET /api/v1/admin/listening/exercises`: supports `topicId` (parent scope) and `q` (search).
  - `GET /api/v1/admin/listening/lessons`: supports `exerciseId` and/or `topicId` (parent scope) and `q` (search).
  - `GET /api/v1/admin/listening/sentences`: supports `lessonId` and/or `exerciseId` and/or `topicId` (parent scope) and `q` (search).
- Admin CRUD responses MUST include parent references and sequence metadata where applicable.
- Nested parent-child endpoints remain supported for contextual authoring flows and do not replace the mandatory flat tab list/search endpoints.
- Reorder endpoint MUST accept ordered sentence IDs and return persisted order.
- Bulk import endpoint MUST accept an array-of-objects payload and return `successCount`, `failedCount`, and `errors[]` with payload index and message.

#### 2) Learner Retrieval and Playback APIs

- `GET /api/v1/listening/topics`
- `GET /api/v1/listening/topics/{topicSlug}/exercises/{exerciseSlug}`
- `GET /api/v1/listening/lessons/{lessonSlug}`
- `GET /api/v1/listening/lessons/{lessonId}/workspace`
- `POST /api/v1/listening/media/access`

**Contract expectations**:
- Workspace payload MUST include lesson metadata, ordered sentences, optional translation values, aliases mapping, learner progress summary, and settings defaults.
- Media access endpoint MUST return pre-signed URL metadata and expiry (TTL 60-300 seconds in phase 1).

#### 3) Progress Update API

- `POST /api/v1/listening/progress/sentences/{sentenceId}`

**Request contract**:
- Action type is restricted to `correct_check` or `skip`.
- Any non-qualifying event (e.g., incorrect check) MUST NOT mark completion.

#### 4) Dictionary Proxy API

- `GET /api/v1/listening/dictionary?term={word}`

**Contract expectations**:
- Backend MUST call Cambridge provider using server-side secret credentials.
- Endpoint MUST return normalized definition payload for frontend display.
- On provider failure, endpoint MUST return controlled failure shape that supports graceful fallback UX.
- Outbound dictionary calls MUST be limited to allowlisted provider hostnames and MUST apply bounded timeout policy.
- Dictionary term input MUST be sanitized/validated before provider calls; invalid terms MUST return controlled validation errors.
- Dictionary proxy MUST enforce response-size and abuse controls to protect service stability.
- Dictionary request/response lifecycle (including failures) MUST include structured logs and correlation ID propagation.

### Dictation Normalization & Matching Rules

For both learner input and expected sentence (including aliases), apply this exact sequence before equality comparison:

1. Apply alias canonicalization map first when configured for the sentence/exercise (for example `"i've" -> "i have"`, `"don't" -> "do not"`).
2. Lowercase conversion.
3. Strip punctuation/symbols/whitespace using regex: `/[‚,.、。！：；）（，？„“‘’”?;:'"\]\[}{!&()\-—+=\s…]/g`.
4. Compare full-string equality only.

Alias handling rules:
- Sentence may define zero or more alias variants.
- Each alias is normalized by the same pipeline.
- Answer is correct when normalized answer equals normalized canonical sentence OR any normalized alias.
- No fuzzy logic, typo tolerance, or token-level partial scoring is allowed.

### Out of Scope (Phase 1)

- Per-attempt history, streak history, or answer-by-answer analytics storage.
- Timestamp validation against actual media duration.
- Additional dictionary providers beyond Cambridge.
- Adaptive difficulty or recommendation engine.

### Key Entities *(include if feature involves data)*

- **Topic**: Top-level listening curriculum container.
- **Exercise**: Group of lessons under a topic, scoped slug uniqueness by topic.
- **Lesson**: Playable learning unit under an exercise, may carry shared media source.
- **Sentence**: Ordered dictation item with canonical text, optional translation, aliases, and media linkage/timestamps.
- **UserSentenceProgress**: Per-user per-sentence completion state and completion source.
- **DictionaryEntry**: Backend-proxied lexical result used for learner lookup display.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of accepted listening content in phase 1 (defined as content records that satisfy phase 1 validation and scope rules, including hierarchy integrity, scoped slug uniqueness, and sentence media/timestamp validity) can be authored and maintained through Admin CMS hierarchy (topic/exercise/lesson/sentence).
- **SC-002**: At least 95% of learners in post-build UAT can complete a full lesson flow (play, answer/check or skip, progress save) without facilitator intervention; this is a UAT outcome metric and not a blocking automated test pass criterion.
- **SC-003**: Dictation correctness decisions are deterministic, with 0% fuzzy/partial acceptance in acceptance test suite.
- **SC-004**: 100% of completion records are created only by correct Check or Skip actions.
- **SC-005**: In dictionary provider outage simulations, learner sessions remain functional with fallback UX and no forced session termination.
- **SC-006**: Sentence ordering changes performed in admin are reflected in learner lesson order on next fetch in 100% of validation cases.

## Assumptions

- Existing authentication and authorization systems are reused for admin/learner role separation.
- Admin and learner surfaces are served on separate routes (`/admin/listening` and `/listening`).
- Audio/media assets are already available in storage compatible with pre-signed URL issuance.
- Alias values are authored by admin as plain text variants per sentence.
- Full transcript view is derived from ordered sentence text within a lesson and follows existing localization behavior.



