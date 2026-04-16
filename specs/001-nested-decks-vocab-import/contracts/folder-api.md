# API Contracts: Folder Management

## Base URL
```
/api/folders
```

## Endpoints

### 1. GET /api/folders/tree

**Description**: Fetch the nested folder tree with aggregated card counts

**Method**: `GET`

**Path**: `/api/folders/tree`

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
Accept: application/json
```

**Query Parameters**: None

**Response (200 OK)**:
```json
[
  {
    "id": 1,
    "name": "IELTS Vocabulary",
    "totalCards": 150,
    "newCards": 50,
    "learningCards": 75,
    "masteredCards": 25,
    "children": [
      {
        "id": 2,
        "name": "Academic",
        "totalCards": 100,
        "newCards": 30,
        "learningCards": 50,
        "masteredCards": 20,
        "children": [
          {
            "id": 3,
            "name": "Science",
            "totalCards": 40,
            "newCards": 10,
            "learningCards": 20,
            "masteredCards": 10,
            "children": []
          }
        ]
      }
    ]
  }
]
```

**Error Responses**:
- `401 Unauthorized`: Missing or invalid JWT token
- `500 Internal Server Error`: Database error

---

### 2. POST /api/folders

**Description**: Create a new folder

**Method**: `POST`

**Path**: `/api/folders`

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "name": "New Folder Name",
  "parent_id": 1
}
```

**Request Fields**:
| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `name` | String | Yes | Non-empty, max 255 chars |
| `parent_id` | Long | No | Must reference valid folder owned by user; null = root |

**Response (201 Created)**:
```json
{
  "id": 5,
  "name": "New Folder Name",
  "totalCards": 0,
  "newCards": 0,
  "learningCards": 0,
  "masteredCards": 0,
  "children": []
}
```

**Error Responses**:
- `400 Bad Request`:
  ```json
  {
    "error": "Folder name cannot be empty",
    "timestamp": "2026-04-15T10:30:00Z"
  }
  ```
  - `"Folder name cannot be empty"`
  - `"Folder name too long (max 255 chars)"`
  - `"Parent folder not found"`
  - `"Circular reference detected"`
  - `"Max depth reached. Folder will be created as sibling at level 6."`

- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Parent folder not found or unauthorized
- `500 Internal Server Error`: Database error

**Behavior**:
- If `parent_id` is at depth level 6, the new folder is automatically promoted to level 6 (sibling of parent)
- No error is thrown; folder is created as sibling transparently
- User isolation enforced: can only reference folders owned by same user

---

### 3. DELETE /api/folders/{id}

**Description**: Delete a folder and cascade delete all descendants and associated flashcards

**Method**: `DELETE`

**Path**: `/api/folders/{id}`

**Path Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | Long | Folder ID to delete |

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

**Response (204 No Content)**:
```
(empty body)
```

**Delete Behavior**:
1. Find folder by ID
2. Identify all descendants recursively
3. Cascade delete all flashcards in deleted folder and descendants
4. Delete all folder records in the branch

**Example**:
```
DELETE parent (with children A, B, C)
→ Parent, A, B, C are deleted
→ All flashcards in parent/A/B/C are deleted
```

**Error Responses**:
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Folder not found or unauthorized
- `409 Conflict`: Folder in use (e.g., active study session)
  ```json
  {
    "error": "Cannot delete folder in use",
    "message": "Exit study mode first"
  }
  ```
- `500 Internal Server Error`: Database error

---

### 4. GET /api/folders/{id}/cards

**Description**: Study mode uses descendant-inclusive card fetch. The current implementation applies this behavior through `/api/v1/study-session/decks/{id}/next-cards` by including cards from `{id}` and all descendants.

**Method**: `GET`

**Path**: `/api/folders/{id}/cards`

**Path Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | Long | Folder ID |

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
Accept: application/json
```

**Query Parameters**:
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `status` | String | (all) | Filter by status: NEW, LEARNING, MASTERED |
| `limit` | Integer | 100 | Max cards per page |
| `offset` | Integer | 0 | Pagination offset |

**Response (200 OK)**:
```json
{
  "folderId": 2,
  "folderName": "Academic",
  "breadcrumb": [
    {
      "id": 1,
      "name": "IELTS Vocabulary"
    },
    {
      "id": 2,
      "name": "Academic"
    }
  ],
  "cards": [
    {
      "id": 101,
      "word": "Comprehensive",
      "definition": "Complete, including all details",
      "status": "NEW",
      "folderId": 2
    },
    {
      "id": 102,
      "word": "Meticulous",
      "definition": "Showing great attention to detail",
      "status": "LEARNING",
      "folderId": 3
    }
  ],
  "totalCards": 150,
  "pagination": {
    "offset": 0,
    "limit": 100,
    "total": 150
  }
}
```

**Error Responses**:
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Folder not found or unauthorized
- `500 Internal Server Error`: Database error

---

## Error Response Format

All error responses follow this format:

```json
{
  "error": "Error Type or Message",
  "message": "Detailed explanation (optional)",
  "timestamp": "2026-04-15T10:30:00Z",
  "status": 400
}
```

---

## Authentication

All endpoints require JWT bearer token in `Authorization` header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Token should contain:
- `sub`: User ID (string)
- `exp`: Expiration time
- `iat`: Issued at time

User ID extracted from JWT token is used to filter all queries (user isolation).

---

## Rate Limiting (Future)

Current implementation has no rate limits. Future enhancement:
- `X-RateLimit-Limit`: Max requests per minute
- `X-RateLimit-Remaining`: Requests left
- `X-RateLimit-Reset`: Unix timestamp when limit resets
- `429 Too Many Requests`: Rate limit exceeded

---

## Pagination (Study Mode Only)

The GET `/api/folders/{id}/cards` endpoint supports pagination:

```json
{
  "pagination": {
    "offset": 0,
    "limit": 100,
    "total": 150
  }
}
```

**Usage**:
```
GET /api/folders/2/cards?limit=50&offset=50
```

---

## Testing with cURL

### List all folders
```bash
curl -H "Authorization: Bearer $JWT_TOKEN" \
  http://localhost:8080/api/folders/tree
```

### Create folder
```bash
curl -X POST http://localhost:8080/api/folders \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "New Folder", "parent_id": null}'
```

### Delete folder
```bash
curl -X DELETE http://localhost:8080/api/folders/5 \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Fetch cards for study
```bash
curl -H "Authorization: Bearer $JWT_TOKEN" \
  "http://localhost:8080/api/folders/2/cards?status=NEW&limit=50"
```

---

