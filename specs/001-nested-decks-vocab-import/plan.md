# Implementation Plan: Folder Tree View

**Branch**: `001-nested-decks-vocab-import` | **Date**: 2026-04-15 | **Spec**: `/specs/001-nested-decks-vocab-import/spec.md`
**Input**: Feature specification from `/specs/001-nested-decks-vocab-import/spec.md`

## Summary

Refactor the flashcard folder management system from a flat structure to a nested tree view (up to tier 6 depth). The UI displays a collapsible folder tree with aggregated card counts (New, Learning, Mastered) and supports study mode for a folder plus all descendants. The frontend includes inline folder creation in the tree when the 'New Folder' action is triggered and folder deletion via a trash icon. The backend computes the nested tree in-memory with parent/child traversal to avoid recursive DB queries and implements cascade deletion plus descendant-aware study fetches.

## Technical Context

**Language/Version**: Java 17, TypeScript 5+
**Primary Dependencies**: Spring Boot, JPA, React 
**Storage**: PostgreSQL (or existing relational DB - Adjacency List `parent_id`)
**Testing**: JUnit, React Testing Library, Vitest
**Target Platform**: Web application (Frontend + Backend)
**Project Type**: Web application
**Performance Goals**: Fast in-memory tree building; avoid N+1 DB queries  
**Constraints**: Structure limited to a maximum depth of 6 levels.  
**Scale/Scope**: Per-user tree evaluation; typical users have limited folder count, safe for in-memory operations.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- UI Style: Minimalist IDE-style, toggle interactions only on arrows. Includes inline input for new folder creation and trash icon deletion.
- DB Pattern: Adjacency list mapping with a self-referencing foreign key. Folder deletion triggers cascade deletion of child folders and flashcards.
- Performance: Aggregations are evaluated in RAM instead of N+1 database queries.

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
│   │   │   ├── entity/        # Deck, Card entities
│   │   │   ├── repository/    # DeckRepository, CardRepository
│   │   │   ├── service/deck/  # FolderService (tree building logic)
│   │   │   └── controller/folder/ # FolderController
│   └── test/                  # Unit tests for tree builder

frontend/
├── src/
│   ├── components/            # FolderTreeView, FolderTreeNode (updated with inline creation & deletion UI)
│   ├── services/              # API client for /api/folders/tree, POST /api/folders, DELETE /api/folders/{id}
│   ├── store/                 # Zustand store updates
│   └── features/study-workspace/ # Page wiring for tree + study mode navigation
└── tests/                     # Component testing
```

**Structure Decision**: Web application (Frontend + Backend) structure selected, with code split across standard Spring Boot layers and React components.

**Terminology Note**: The implementation uses `Deck/Card` entities in code while the feature spec uses `Folder/Flashcard` terminology for the product surface.

## Complexity Tracking

No open complexity exceptions remain for this feature.
