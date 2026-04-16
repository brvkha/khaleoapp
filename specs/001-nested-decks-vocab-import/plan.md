# Implementation Plan: Folder Tree View

**Branch**: `001-nested-decks-vocab-import` | **Date**: 2026-04-15 | **Spec**: `/specs/001-nested-decks-vocab-import/spec.md`
**Input**: Feature specification from `/specs/001-nested-decks-vocab-import/spec.md`

## Summary

Refactor the flashcard folder management system from a flat structure to a nested tree view (up to tier 6 depth). The UI will display a collapsible folder tree with aggregated card counts (New, Learning, Mastered) and enable study modes encompassing a folder and all its descendants. The frontend will also introduce new UI for inline folder creation (displaying an input field directly in the tree when the 'New Folder' action is triggered) and support for folder deletion via a trash icon or context menu. The backend computes the nested tree in-memory via Java Stream API to avoid recursive DB queries and implements logic for hard-deleting folders and their descendants (or removing the association with cards).

## Technical Context

**Language/Version**: Java 17, TypeScript 5+
**Primary Dependencies**: Spring Boot, JPA, React 
**Storage**: PostgreSQL (or existing relational DB - Adjacency List `parent_id`)
**Testing**: JUnit, React Testing Library
**Target Platform**: Web application (Frontend + Backend)
**Project Type**: Web application
**Performance Goals**: Fast in-memory tree building; avoid N+1 DB queries  
**Constraints**: Structure limited to a maximum depth of 6 levels.  
**Scale/Scope**: Per-user tree evaluation; typical users have limited folder count, safe for in-memory operations.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- UI Style: Minimalist IDE-style, toggle interactions only on arrows. Includes inline input for new folder creation and trash icon/context menu for deletion.
- DB Pattern: Adjacency list mapping with a self-referencing foreign key. Folder deletion triggers cascade deletion of child folders and/or cascade removal of related flashcards.
- Performance: Must evaluate aggregations in RAM instead of N+1 database queries.

## Project Structure

### Documentation (this feature)

```text
specs/001-nested-decks-vocab-import/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
└── tasks.md             # Phase 2 output
```

### Source Code (repository root)

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/com/khaleo/flashcard/
│   │   │   ├── model/         # Folder, Flashcard entities
│   │   │   ├── repository/    # FolderRepository, FlashcardRepository
│   │   │   ├── service/       # FolderService (tree building logic)
│   │   │   └── controller/    # FolderController
│   └── test/                  # Unit tests for tree builder

frontend/
├── src/
│   ├── components/            # FolderTreeView, FolderTreeNode (updated with inline creation & deletion UI)
│   ├── services/              # API client for /api/folders/tree, POST /api/folders, DELETE /api/folders/{id}
│   └── store/                 # Zustand store updates
└── tests/                     # Component testing
```

**Structure Decision**: Web application (Frontend + Backend) structure selected, with code split across standard Spring Boot layers and React components.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
