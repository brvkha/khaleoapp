# Data Model: Listening Dictation + Admin CMS (Phase 1)

## Entities

### 1) Topic
- Description: Top-level grouping (for example IELTS Listening, TED, News).
- Fields:
  - `id` (UUID, PK)
  - `name` (VARCHAR, required)
  - `slug` (VARCHAR, required, globally unique)
  - `description` (TEXT, optional)
  - `status` (ENUM: `draft|published|archived`, default `draft`)
  - `created_at` (TIMESTAMP, required)
  - `updated_at` (TIMESTAMP, required)

### 2) Exercise
- Description: Collection unit under one topic (for example Cambridge 20).
- Fields:
  - `id` (UUID, PK)
  - `topic_id` (UUID, FK -> `topics.id`, required)
  - `name` (VARCHAR, required)
  - `slug` (VARCHAR, required, unique within `topic_id`)
  - `order_index` (INT, required)
  - `status` (ENUM: `draft|published|archived`, default `draft`)
  - `created_at` (TIMESTAMP, required)
  - `updated_at` (TIMESTAMP, required)
- Constraints:
  - `UNIQUE(topic_id, slug)`

### 3) Lesson
- Description: Playable listening lesson under one exercise (for example Test 1 - Part 1).
- Fields:
  - `id` (UUID, PK)
  - `exercise_id` (UUID, FK -> `exercises.id`, required)
  - `name` (VARCHAR, required)
  - `slug` (VARCHAR, required, unique within `exercise_id`)
  - `order_index` (INT, required)
  - `media_url` (VARCHAR(2048), nullable; shared source media object key/url)
  - `media_type` (ENUM: `audio|video`, nullable)
  - `status` (ENUM: `draft|published|archived`, default `draft`)
  - `created_at` (TIMESTAMP, required)
  - `updated_at` (TIMESTAMP, required)
- Constraints:
  - `UNIQUE(exercise_id, slug)`

### 4) Sentence
- Description: Ordered dictation sentence in a lesson with hybrid media references.
- Fields:
  - `id` (UUID, PK)
  - `lesson_id` (UUID, FK -> `lessons.id`, required)
  - `order_index` (INT, required)
  - `transcript` (TEXT, required)
  - `translation` (TEXT, nullable)
  - `aliases_json` (JSON, nullable; canonicalization map and/or accepted alias variants)
  - `media_url` (VARCHAR(2048), nullable; sentence-specific media)
  - `start_time` (DECIMAL(10,3), nullable)
  - `end_time` (DECIMAL(10,3), nullable)
  - `created_at` (TIMESTAMP, required)
  - `updated_at` (TIMESTAMP, required)
- Validation:
  - `transcript` required and non-blank.
  - Shared lesson-media mode requires `start_time >= 0` and `end_time > start_time`.
  - Phase 1 does not validate `end_time` against actual media duration.

### 5) UserSentenceProgress
- Description: Per-user progress state for one sentence.
- Fields:
  - `id` (UUID, PK)
  - `user_id` (UUID, FK -> `users.id`, required)
  - `sentence_id` (UUID, FK -> `sentences.id`, required)
  - `is_completed` (BOOLEAN, required)
  - `completion_source` (ENUM: `correct_check|skip`, required)
  - `completed_at` (TIMESTAMP, nullable)
  - `updated_at` (TIMESTAMP, required)
- Constraints:
  - `UNIQUE(user_id, sentence_id)`
- Business rule:
  - Upsert only when answer is correct after normalization/alias matching or when Skip action is used.

### 6) DictionaryCache (optional in phase 1, recommended)
- Description: Backend cache for Cambridge responses to reduce quota usage.
- Fields:
  - `id` (UUID, PK)
  - `term` (VARCHAR, required, normalized lowercase key)
  - `provider` (VARCHAR, required; `cambridge`)
  - `payload_json` (JSON/TEXT, required)
  - `expires_at` (TIMESTAMP, required)
  - `created_at` (TIMESTAMP, required)

## Relationships
- `topics (1) -> (N) exercises`
- `exercises (1) -> (N) lessons`
- `lessons (1) -> (N) sentences`
- `users (1) -> (N) user_sentence_progress`
- `sentences (1) -> (N) user_sentence_progress`

## State transitions
- Sentence progress lifecycle:
  - `not_started` -> `completed` via `correct_check` or `skip`
  - incorrect `check` keeps current state unchanged.
- Lesson progress calculation:
  - `progress_percent = completed_sentence_count / total_sentences * 100`
- Admin reorder lifecycle:
  - drag-drop in UI -> ordered IDs payload -> backend rewrites `order_index` transactionally for that lesson.

