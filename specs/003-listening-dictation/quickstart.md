# Quickstart: Listening Dictation + Admin CMS (Phase 1)

## Goal
Deliver end-to-end listening dictation with:
- Admin drill-down CMS (`Topic -> Exercise -> Lesson -> Sentence`) including sentence drag-drop reorder and JSON import.
- Learner workspace with Dictation + Full Transcript tabs, media player controls, dictionary popup, settings, and progress tracking.

## 1) Backend implementation outline
1. Add Flyway migrations for listening domain tables:
   - `topics`, `exercises`, `lessons`, `sentences`, `user_sentence_progress`.
   - Include scoped unique indexes: `(topic_id, slug)` for exercises and `(exercise_id, slug)` for lessons.
2. Implement Admin CMS APIs:
   - CRUD for topics/exercises/lessons/sentences.
   - `PUT /admin/listening/lessons/{lessonId}/sentences/reorder`.
   - `POST /admin/listening/lessons/{lessonId}/sentences/import-json` (partial success response).
3. Implement learner APIs:
   - Lesson workspace retrieval with ordered sentences + progress summary.
   - Progress update endpoint with action guard (`correct_check` or `skip`).
4. Implement media access exchange endpoint:
   - Return short-lived presigned S3 URL metadata.
5. Implement dictionary proxy endpoint:
   - Backend handles Cambridge key and optional cache.
   - Return controlled fallback payload on provider failure.

## 2) Frontend implementation outline
1. Replace placeholder `ListeningPage` with learner workspace shell.
2. Build learner components:
   - Dictation tab (textarea, Enter=Check, Esc=Skip, previous/next controls).
   - Full Transcript tab (click-to-play, optional auto-scroll, optional loop).
   - Shared media player with speed controls (`0.25x` to `3.0x`).
3. Implement dictation evaluator:
   - Apply exact normalization + alias mapping pipeline from `spec.md`.
   - Render UI masking rule (green correct prefix, `***` from first mismatch onward).
4. Implement media flow:
   - Request presigned URL.
   - Fetch media bytes as blob/arraybuffer.
   - Play via `URL.createObjectURL()`.
5. Build settings modal with localStorage persistence:
   - Replay key, play/pause key, auto play on next, auto replay count, replay interval.
6. Build dictionary popup UX:
   - Clickable words in transcript/answer views.
   - Graceful fallback when proxy/provider fails.

## 3) Testing checklist
- Backend tests:
  - Scoped slug uniqueness constraints.
  - Sentence timestamp validation (`start_time >= 0`, `end_time > start_time`).
  - Reorder transaction correctness.
  - JSON import partial success contract.
  - Progress update guard (ignore incorrect checks).
  - Dictionary proxy success/failure contract.
- Frontend tests:
  - Normalization + alias exact-match behavior.
  - Left-to-right masking UI rule.
  - Keyboard shortcuts and settings persistence.
  - Transcript auto-scroll + loop behavior.
  - Presigned URL fetch and blob playback pipeline.
- Integration/E2E:
  - Admin create hierarchy -> learner consumes same lesson.
  - Import -> reorder -> learner playback sequence validation.
  - Progress percentage updates after correct/skip actions.

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

## 5) Troubleshooting notes (phase 3)
- Backend tests require an active MySQL instance reachable by the `test` profile.
- If Flyway fails with `Communications link failure`, start local DB first:

```powershell
Set-Location C:\Workspace\FPT\khaleoapp\backend
.\scripts\reset-local-database.ps1
```

- If listening media playback fails in learner UI, verify:
  - `LISTENING_MEDIA_PRESIGNED_URL_TTL_SECONDS` is non-zero.
  - Media object key/url stored in lesson/sentence is valid.
- Dictionary fallback is expected when provider is unavailable; learner flow should continue.

## 6) Verification notes (phase 3)
- US2 frontend unit/component tests:

```powershell
Set-Location C:\Workspace\FPT\khaleoapp\frontend
npm test -- src/test/listening/dictationEvaluator.test.ts src/test/listening/listeningDictationFlow.test.tsx
```

- Listening e2e smoke (currently scaffolded + skipped until stable seeded auth fixture):

```powershell
Set-Location C:\Workspace\FPT\khaleoapp\frontend
npm run test:e2e -- --grep "listening"
```

