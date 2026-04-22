# Implementation Plan: Listening Dictation + Admin CMS (Phase 1)

**Branch**: `003-listening-dictation` | **Date**: 2026-04-21 | **Spec**: `/specs/003-listening-dictation/spec.md`
**Input**: Feature specification from `/specs/003-listening-dictation/spec.md`

## Summary

Implement a full listening module in phase 1 covering both Admin CMS and learner workspace. Admin supports drill-down CRUD (`Topic -> Exercise -> Lesson -> Sentence`), sentence drag-drop reorder, and JSON import with partial success reporting. Learner workspace supports strict dictation checking (exact normalized match + alias mapping), transcript mode, keyboard shortcuts, settings persistence, progress tracking, and dictionary lookup via backend proxy.

Media delivery uses short-lived S3 presigned URLs while frontend plays fetched blob/object URLs to reduce direct origin exposure. Progress is persisted only for `correct_check` and `skip`; attempt history and fuzzy scoring remain out of scope.

## Technical Context

**Language/Version**: Java 17 (Spring Boot 3.3.x), TypeScript 5.9 + React 19  
**Primary Dependencies**: Spring Web/Data JPA/Validation/Security, Flyway, MySQL connector, AWS SDK S3, React Router, Zustand, Axios, DOMPurify  
**Storage**: MySQL (listening master data + user sentence progress), S3 for media objects, browser localStorage for learner settings  
**Testing**: JUnit 5 + Spring Boot/Testcontainers (backend), Vitest + React Testing Library (frontend), Playwright e2e (frontend)  
**Target Platform**: Web app (browser frontend + Spring Boot backend)
**Project Type**: Monorepo web application (`backend/` + `frontend/`)  
**Performance Goals**: p95 learner API response (lesson detail, progress read/write, media URL exchange) <= 300ms under normal load; p95 UI reaction (playback controls, sentence transition feedback) < 150ms after user input; admin sentence reorder persistence < 2s p95 for lessons up to 300 sentences; admin JSON import (up to 1,000 sentences / 5 MB payload) completes with result summary < 10s p95  
**Constraints**: Strict non-fuzzy dictation match, completion only via `correct_check|skip`, sentence timestamp validation limited to `start_time >= 0` and `end_time > start_time`, dictionary key server-side only, media via presigned URL exchange + blob playback  
**Scale/Scope**: New listening schema/entities/APIs plus learner/admin UI surfaces for one full content hierarchy and lesson playback workflow in phase 1

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- Constitution file is ratified at `.specify/memory/constitution.md` (v1.0.0).
- Applicable mandatory gates for this feature:
  - Contract integrity: backend/frontend API and DTO behavior must remain synchronized and covered by contract tests.
  - Security: dictionary provider key remains backend-only; media is issued via time-limited pre-signed URL; failure fallback does not block learner flow.
  - Test quality: unit + integration/contract coverage for normalization, reorder/import, progress upsert semantics, and dictionary fallback.
  - Performance evidence: tasks must include explicit verification against the measurable p95 targets in this plan.
- **Gate status before Phase 0**: PASS.
- **Gate status after Phase 1 design**: PASS (research/data-model/contracts/quickstart/tasks align with constitution gates).

## Phase 0 Research Output

- `research.md` resolves technology and integration decisions for hierarchy modeling, reorder/import contracts, hybrid media playback, strict dictation matching, dictionary proxy handling, and settings persistence.

## Phase 1 Design Output

- `data-model.md` defines entities, fields, constraints, and transitions for listening content and progress.
- `contracts/listening-dictation.openapi.yaml` defines Admin CMS, learner workspace, progress, media access, and dictionary proxy interfaces.
- `quickstart.md` defines implementation order, verification checklist, and local validation commands.

## Project Structure

### Documentation (this feature)

```text
specs/003-listening-dictation/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── listening-dictation.openapi.yaml
└── tasks.md
```

### Source Code (repository root)

```text
backend/
└── src/
   ├── main/java/com/khaleo/flashcard/
   │  ├── controller/admin/listening/
   │  ├── controller/listening/
   │  ├── controller/listening/dto/
   │  ├── entity/
   │  ├── repository/
   │  └── service/listening/
   ├── main/resources/db/migration/
   └── test/java/com/khaleo/flashcard/
      ├── contract/listening/
      ├── integration/listening/
      └── unit/listening/

frontend/
├── src/
│  ├── features/listening/
│  │  ├── components/
│  │  ├── hooks/
│  │  ├── services/
│  │  ├── utils/
│  │  └── ListeningPage.tsx
│  ├── features/admin/listening/
│  ├── services/
│  ├── store/
│  └── test/
│     ├── listening/
│     └── admin/
└── tests/
   └── e2e/
```

**Structure Decision**: Keep the existing monorepo split and add listening-specific backend/frontend modules without introducing new top-level packages.

## Complexity Tracking

No constitution exceptions or complexity waivers are required for this plan.
