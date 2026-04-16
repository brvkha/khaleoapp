# API Contracts: Folder Tree View

## Endpoints Modified

### 1. `GET /api/folders/tree`
- Returns a recursive tree of folders for the authenticated user, complete with nested children and aggregated sums.
- Response contract:
```json
[
  {
    "id": 1,
    "name": "IELTS",
    "totalCards": 150,
    "newCards": 50,
    "learningCards": 80,
    "masteredCards": 20,
    "children": [
      {
        "id": 2,
        "name": "Cam 7",
        "totalCards": 80,
        "newCards": 30,
        "learningCards": 40,
        "masteredCards": 10,
        "children": [] 
      }
    ]
  }
]
```

### 2. `POST /api/folders`
- Optional `parent_id` accepted in request body.
- Used to support the new inline folder creation capability from the UI tree view.
- The backend validates if the provided parent folder is at max depth (6). If so, it assigns the new folder to the parent's `parent_id` (creating a sibling).
- Payload contract:
```json
{
  "name": "IELTS Reading",
  "parent_id": 1
}
```

### 3. `DELETE /api/folders/{id}`
- Triggers a cascaded hard deletion of the specified folder by its `id`.
- This will cascade delete any child folders stored with `parent_id` equal to this folder's `id`, recursively.
- It will also drop association with (or delete) any Flashcards housed within this folder branch.

### 4. `GET /api/study/folder/{id}` (or similar)
- Changed business logic: Will return flashcards not only natively belonging to the requested `id`, but all flashcards of folders recursively descendent from `{id}`. Payload schema remains unchanged.
