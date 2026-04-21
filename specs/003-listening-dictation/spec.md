# Feature Specification: Listening Dictation + Admin CMS (Phase 1)

**Feature Branch**: `003-listening-dictation`  
**Created**: 2026-04-21  
**Status**: Draft  
**Input**: User description: "Create a new feature specification for khaleoapp for BIG FEATURE 'Listening Dictation' using the user's finalized decisions."

## Clarifications

### Session 2026-04-21

- Include both learner-facing dictation workspace and full Admin CMS in phase 1.
- Admin hierarchy is Topic -> Exercise -> Lesson -> Sentence with CRUD for all levels.
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

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Manage Listening Content in Admin CMS (Priority: P1)

As an admin/content manager, I can create and maintain listening curriculum content (topics, exercises, lessons, sentences) so learners can practice dictation with structured, publishable materials.

**Why this priority**: Without content authoring and maintenance, learner dictation cannot operate in production.

**Independent Test**: Can be tested independently by creating one topic, one exercise, one lesson, adding/reordering sentences, importing sentence JSON, and verifying data is retrievable by learners.

**Acceptance Scenarios**:

1. **Given** an admin is in CMS, **When** they create topic, exercise, lesson, and sentence records with valid required fields, **Then** records are saved and visible in the hierarchy.
2. **Given** a lesson has multiple sentences, **When** admin drag-drops sentence order and saves, **Then** learner delivery reflects the updated order.
3. **Given** admin uploads valid sentence JSON payload, **When** bulk import is submitted, **Then** valid rows are created/updated and invalid rows are returned with row-level validation errors.
4. **Given** an exercise slug already exists inside the same topic, **When** admin reuses that slug in that topic, **Then** the request is rejected as duplicate scope conflict.

---

### User Story 2 - Complete Dictation Practice in Learner Workspace (Priority: P1)

As a learner, I can listen to sentence audio and submit dictation answers so I can practice listening accuracy and track completion.

**Why this priority**: This is the core learner value of the Listening Dictation feature.

**Independent Test**: Can be tested by opening an assigned lesson, playing sentence audio, checking answers, skipping where needed, and verifying completion states are saved.

**Acceptance Scenarios**:

1. **Given** learner enters an answer, **When** they press Check (Enter), **Then** the answer is marked correct only if it exactly matches the normalized target or configured alias mapping output.
2. **Given** learner presses Skip (Esc), **When** skip action is accepted, **Then** sentence is marked completed and next sentence is available.
3. **Given** learner presses replay shortcut (default Ctrl) or play/pause shortcut (default backtick), **When** media is available, **Then** playback control executes without mouse interaction.
4. **Given** sentence translation is null, **When** learner opens sentence details, **Then** UI shows configured fallback text or hides translation section without blocking dictation flow.

---

### User Story 3 - Use Transcript, Dictionary, and Settings for Better Learning (Priority: P2)

As a learner, I can use full transcript, inline dictionary lookup, and personal playback settings so I can understand content and keep a consistent study rhythm.

**Why this priority**: These capabilities improve comprehension and usability but are secondary to core dictation correctness.

**Independent Test**: Can be tested by toggling transcript visibility, requesting dictionary definitions, changing Auto Play on Next, and confirming progress continuity.

**Acceptance Scenarios**:

1. **Given** learner requests dictionary meaning for a word, **When** backend dictionary proxy succeeds, **Then** definition data is displayed to learner.
2. **Given** dictionary provider is unavailable, **When** lookup fails, **Then** frontend displays graceful fallback guidance and dictation session remains usable.
3. **Given** learner enables Auto Play on Next, **When** they complete a sentence and advance, **Then** next sentence playback starts automatically.
4. **Given** learner revisits an in-progress lesson, **When** workspace loads, **Then** completion state from prior correct/skip actions is reflected.

---

### Edge Cases

- Lesson has no sentences: learner sees an empty-state message and no playback controls.
- Sentence has invalid shared-media timestamps (`start_time < 0` or `end_time <= start_time`): admin save/import is rejected with field-level errors.
- Shared media exists but pre-signed URL expires before playback: learner receives refresh/retry flow for a new URL exchange.
- Sentence-level translation is missing: translation panel is hidden or fallback message shown per UI rule.
- Learner submits answer with extra punctuation, spacing, or case differences: normalization resolves these differences before exact match.
- Alias mapping changes in CMS after learner already completed a sentence: existing completion status remains unchanged unless learner retries sentence.
- Dictionary API rate-limited or unavailable: fallback is shown without blocking lesson progression.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001 (Phase 1 Scope)**: System MUST deliver both learner module and full Admin CMS for Listening Dictation in phase 1.
- **FR-002 (Admin Topic CRUD)**: System MUST provide create/read/update/delete operations for `topics` with unique topic slug at global scope.
- **FR-003 (Admin Exercise CRUD)**: System MUST provide create/read/update/delete operations for `exercises` linked to a parent topic.
- **FR-004 (Exercise Slug Scope Rule)**: System MUST enforce exercise slug uniqueness within the same topic (duplicate allowed across different topics).
- **FR-005 (Admin Lesson CRUD)**: System MUST provide create/read/update/delete operations for `lessons` linked to a parent exercise.
- **FR-006 (Lesson Slug Scope Rule)**: System MUST enforce lesson slug uniqueness within the same exercise (duplicate allowed across different exercises).
- **FR-007 (Admin Sentence CRUD)**: System MUST provide create/read/update/delete operations for `sentences` linked to a parent lesson.
- **FR-008 (Sentence Reorder)**: System MUST allow admin to reorder sentences via drag-drop and persist deterministic sequence order.
- **FR-009 (Sentence JSON Bulk Import)**: System MUST support JSON bulk import for sentences with per-row validation errors and partial success reporting.
- **FR-010 (Sentence Translation Optionality)**: System MUST allow `sentences.translation` to be null.
- **FR-011 (Translation Fallback Rendering)**: Learner UI MUST hide translation section or show fallback text when sentence translation is null.
- **FR-012 (Hybrid Media Strategy)**: System MUST support sentence media through either sentence-specific media reference or shared lesson media with sentence timestamps.
- **FR-013 (Shared Media Timestamp Validation)**: For shared lesson media mode, system MUST validate `start_time >= 0` and `end_time > start_time`.
- **FR-014 (No Duration Validation in Phase 1)**: System MUST NOT validate timestamps against actual media duration in phase 1.
- **FR-015 (Pre-Signed Media Access)**: System MUST deliver learner media through short-lived pre-signed URL exchange.
- **FR-016 (Frontend Playback Source Rule)**: Learner client MUST fetch media bytes (blob/arraybuffer) and use object URL playback source.
- **FR-017 (Dictation Answer Rule)**: System MUST mark Check action as correct only when learner answer exactly equals normalized target sentence or normalized alias mapping output.
- **FR-017b (UI Error Masking Rule)**: When Check action is evaluated, the UI MUST perform a left-to-right word match. It MUST display correctly typed words, stop at the first incorrect word, and mask the incorrect word along with all remaining un-typed words using asterisks (***).
- **FR-018 (No Fuzzy Matching)**: System MUST NOT apply fuzzy, phonetic, edit-distance, or partial-match scoring in phase 1 correctness.
- **FR-019 (Normalization Standard)**: System MUST normalize learner answer and target text before comparison using the approved normalization pipeline and punctuation stripping regex.
- **FR-020 (Completion Tracking Rule)**: System MUST mark sentence completion only on (a) correct Check or (b) explicit Skip action.
- **FR-021 (No Attempt History in Phase 1)**: System MUST NOT persist per-try attempt history in phase 1.
- **FR-022 (User Sentence Progress Persistence)**: System MUST persist completion state in `user_sentence_progress` keyed by user and sentence.
- **FR-023 (Learner Workspace Components)**: Learner workspace MUST include dictation input/check flow, full transcript view, audio player controls, personal settings, and progress indicators.
- **FR-023b (Transcript Auto-Scroll)**: When playing media in the Full Transcript tab, the UI MUST automatically scroll to keep the currently playing sentence pinned around the 3rd line of the visible viewport.
- **FR-024 (Auto Play Setting)**: System MUST provide a user-selectable `Auto Play on Next` setting and apply it when advancing to next sentence.
- **FR-025 (Keyboard Shortcuts)**: Learner workspace MUST support default shortcuts: Enter=Check, Esc=Skip, Ctrl=Replay, backtick=Play/Pause.
- **FR-026 (Auto Replay Controls)**: Settings MUST support configurable auto replay count per sentence (None, 1, 2, Infinite) and replay interval options (for example 0.5s, 1.0s, 1.5s).
- **FR-027 (Full Transcript Playback Controls)**: Full Transcript tab MUST support click-to-play by sentence and optional loop/repeat for full lesson playback.
- **FR-028 (Clickable Word Lookup)**: In answer-visible and transcript contexts, English words MUST be clickable to request dictionary lookup.
- **FR-029 (Dictionary Proxy)**: System MUST provide dictionary lookup via backend proxy to Cambridge provider with provider key hidden server-side.
- **FR-030 (Dictionary Fallback UX)**: If dictionary provider fails, learner UI MUST show graceful fallback messaging and keep dictation flow available.
- **FR-031 (Learner Retrieval API)**: System MUST provide learner-facing retrieval APIs for topic/exercise/lesson/sentence structures and current learner progress.
- **FR-032 (Progress Update API)**: System MUST provide endpoint(s) to update completion state for correct-check and skip actions.
- **FR-033 (Pre-Signed URL Exchange API)**: System MUST provide endpoint(s) for issuing short-lived media access URLs for lesson or sentence media.

### Data Model Requirements

- **FR-034 (`topics` table)**: `topics` MUST store identity, display metadata, slug, lifecycle status, and audit timestamps.
- **FR-035 (`exercises` table)**: `exercises` MUST store parent `topic_id`, identity, display metadata, slug, status, and audit timestamps.
- **FR-036 (`lessons` table)**: `lessons` MUST store parent `exercise_id`, identity, display metadata, slug, summary fields, optional shared media reference, status, and audit timestamps.
- **FR-037 (`sentences` table)**: `sentences` MUST store parent `lesson_id`, ordered position, canonical text, optional translation, optional aliases mapping, media mode fields (sentence media reference and/or shared-media timestamps), and audit timestamps.
- **FR-038 (`user_sentence_progress` table)**: `user_sentence_progress` MUST store `user_id`, `sentence_id`, completion flag/status, completion source (`correct_check` or `skip`), completion timestamp, and last-updated timestamp.
- **FR-039 (Progress Uniqueness)**: `user_sentence_progress` MUST enforce one progress row per `(user_id, sentence_id)`.
- **FR-040 (Scoped Slug Constraints)**: Data model MUST enforce scoped uniqueness constraints for exercise and lesson slug rules described in FR-004 and FR-006.

### API Contract

#### 1) Admin CMS APIs

- `GET /api/v1/admin/listening/topics`
- `POST /api/v1/admin/listening/topics`
- `GET /api/v1/admin/listening/topics/{topicId}`
- `PUT /api/v1/admin/listening/topics/{topicId}`
- `DELETE /api/v1/admin/listening/topics/{topicId}`
- `GET /api/v1/admin/listening/topics/{topicId}/exercises`
- `POST /api/v1/admin/listening/topics/{topicId}/exercises`
- `PUT /api/v1/admin/listening/exercises/{exerciseId}`
- `DELETE /api/v1/admin/listening/exercises/{exerciseId}`
- `GET /api/v1/admin/listening/exercises/{exerciseId}/lessons`
- `POST /api/v1/admin/listening/exercises/{exerciseId}/lessons`
- `PUT /api/v1/admin/listening/lessons/{lessonId}`
- `DELETE /api/v1/admin/listening/lessons/{lessonId}`
- `GET /api/v1/admin/listening/lessons/{lessonId}/sentences`
- `POST /api/v1/admin/listening/lessons/{lessonId}/sentences`
- `PUT /api/v1/admin/listening/sentences/{sentenceId}`
- `DELETE /api/v1/admin/listening/sentences/{sentenceId}`
- `PUT /api/v1/admin/listening/lessons/{lessonId}/sentences/reorder`
- `POST /api/v1/admin/listening/lessons/{lessonId}/sentences/import-json`

**Contract expectations**:
- Admin CRUD responses MUST include parent references and sequence metadata where applicable.
- Reorder endpoint MUST accept ordered sentence IDs and return persisted order.
- Bulk import endpoint MUST return `successCount`, `failedCount`, and `errors[]` with row index and message.

#### 2) Learner Retrieval and Playback APIs

- `GET /api/v1/listening/topics`
- `GET /api/v1/listening/topics/{topicSlug}/exercises/{exerciseSlug}`
- `GET /api/v1/listening/lessons/{lessonSlug}`
- `GET /api/v1/listening/lessons/{lessonId}/workspace`
- `POST /api/v1/listening/media/access`

**Contract expectations**:
- Workspace payload MUST include lesson metadata, ordered sentences, optional translation values, aliases mapping, learner progress summary, and settings defaults.
- Media access endpoint MUST return short-lived pre-signed URL metadata and expiry.

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

### Dictation Normalization & Matching Rules

The system utilizes two distinct pipelines to prevent normalization from breaking visual word boundaries:

**1. Correctness Evaluation Pipeline (Strict Match):**
Applied to both learner input and expected sentence (including aliases) before equality check:
1. Unicode normalization to NFKC.
2. Lowercase conversion.
3. Punctuation/symbol stripping using regex: `/[\p{P}\p{S}]/gu`.
4. Collapse internal whitespace to single spaces using `/\s+/g`.
5. Trim leading/trailing spaces.
6. Compare full-string equality. No fuzzy logic or partial scoring is allowed.

**2. UI Visual Masking Pipeline (Pre-normalized Tokenization):**
Applied strictly for rendering the `***` mask in the UI (FR-017b):
1. Tokenize the *original, un-normalized* transcript and learner input by spaces (preserving punctuation attached to words).
2. Perform a left-to-right token comparison (case-insensitive, ignoring punctuation for the match logic).
3. Display correctly matched tokens as typed.
4. Stop at the first mismatched token. Mask that token and all subsequent tokens with asterisks (`***`).

### Out of Scope (Phase 1)

- Per-attempt history, streak history, or answer-by-answer analytics storage.
- Fuzzy matching, phonetic matching, or AI-assisted correction.
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

- **SC-001**: 100% of accepted listening content in phase 1 can be authored and maintained through Admin CMS hierarchy (topic/exercise/lesson/sentence).
- **SC-002**: At least 95% of learners in UAT can complete a full lesson flow (play, answer/check or skip, progress save) without facilitator intervention.
- **SC-003**: Dictation correctness decisions are deterministic, with 0% fuzzy/partial acceptance in acceptance test suite.
- **SC-004**: 100% of completion records are created only by correct Check or Skip actions.
- **SC-005**: In dictionary provider outage simulations, learner sessions remain functional with fallback UX and no forced session termination.
- **SC-006**: Sentence ordering changes performed in admin are reflected in learner lesson order on next fetch in 100% of validation cases.

## Assumptions

- Existing authentication and authorization systems are reused for admin/learner role separation.
- Existing `/listening` route and placeholder page are replaced by this feature without introducing a second route.
- Audio/media assets are already available in storage compatible with pre-signed URL issuance.
- Alias values are authored by admin as plain text variants per sentence.
- Full transcript view is derived from ordered sentence text within a lesson and follows existing localization behavior.



