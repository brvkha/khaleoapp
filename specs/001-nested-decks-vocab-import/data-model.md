# Data Model: Nested Decks

## Entities extracted

### 1. Folder (formerly Deck)
- **Description**: Container for learning cards. Can now be nested beneath a parent folder.
- **Fields (New/Modified)**:
  - `id` (Long, PK)
  - `name` (String, Required)
  - `parent_id` (Long, FK to `Folder.id`, Optional) - Creates hierarchy structure.
- **Validation**:
  - `parent_id` must exist.
  - Limits maximum depth to 6 levels. If the passed `parent_id` is already at level 6, it uses the parent's `parent_id` to make it a sibling.
- **Relationships**:
  - `ManyToOne` to itself (parent).
  - `OneToMany` to cards (Flashcard).

### 2. Flashcard (formerly Card)
- **Description**: The vocabulary item belonging to a Folder.
- **Fields (New/Modified)**:
  - `id` (Long, PK)
  - `word` (String)
  - `status` (Enum: NEW, LEARNING, MASTERED)
  - `folder_id` (Long, FK to `Folder.id`, Required)
- **Relationships**:
  - Belongs to a single Folder. (For study sessions of a parent folder, cards of its descendants are collected).

## State Transitions
- **Folder Deletion**: 
  - Initiated via a trash icon or Context Menu in the UI. 
  - When a parent folder is deleted, the backend performs a cascaded hard delete of all descendant folders. 
  - Corresponding flashcards belonging to the deleted folder(s) will also be deleted (cascade delete). If requirements dictate otherwise, they could be moved to an 'Uncategorized' state or prompt the user. Assumed safe default: Hard delete with cascade.
  - The UI tree structure immediately updates to reflect the removal of the specific node and all its nested children.
- **Folder Creation**: New functionality allows for inline creation (a text box directly below or within the tree) to specify the new folder's `name` and visual location under its parent. Its `parent_id` is automatically linked to the folder on which the "New Folder" icon was clicked, unless its depth exceeds 6, where it safely falls back to becoming a sibling.
