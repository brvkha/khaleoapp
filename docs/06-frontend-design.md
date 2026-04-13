# 06 - Frontend Design (React + Tailwind)

## Pham vi

3 tab chính: `Decks`, `Cards`, `Study` bám theo logic reference, dùng React + Tailwind + TypeScript.

## 1. Tab DECKS - Quản lý Deck

### State
```typescript
type DeckUI = {
  id: string
  name: string
  description: string | null
  stats: { learning: number; review: number; new_cards: number }
}

type PageState = {
  decks: DeckUI[]
  loading: boolean
  error: string | null
  success: string | null
  searchInput: string
  viewMode: 'card' | 'table'
}
```

### UI
- Header: "Decks" + Search + "Create Deck" button + View toggle (Card | Table)
- **Card view:** Grid 2 cột, mỗi card show name + description + 3 badges (Learning/Review/New)
- **Table view:** Cột Name | Description | Learning | Review | New | Action
- Action buttons: "Start Study" → `/flashcard/study/session/{deckId}`, Edit, Delete
- Empty state: "No decks yet. Create your first!"

### API
```
GET /api/v1/private/decks?q={q}&page=0&size=50
POST /api/v1/private/decks { name, description }
PUT /api/v1/private/decks/{id} { name, description }
DELETE /api/v1/private/decks/{id}
GET /api/v1/private/decks/{id}/stats
```

---

## 2. Tab CARDS - Quản lý Card

### 2-Panel Layout
- **Left:** Deck list (click to select)
- **Right:** Cards table (search + pagination)

### UI
- Left: "Private Decks" header + "+ Create Deck" button + scrollable deck list
- Right: Selected deck name + "{count} cards" + search bar (front/back) + "+ Add Card" button + table
- **Table:** # | Front (truncate) | Back (truncate) | Action
- **Inline edit:** Click "Update" → replace row with input fields
- **Pagination:** Page size (20/50/100) + Prev/Next buttons

### API
```
GET /api/v1/private/decks?q=&page=0&size=50
GET /api/v1/private/decks/{id}/cards/search?frontText={q}&backText={q}&page={p}&size={sz}
POST /api/v1/decks/{id}/cards { term, answer }
PUT /api/v1/cards/{id} { term, answer, version }
DELETE /api/v1/cards/{id}
```

---

## 3. Tab STUDY - Học tập với FSRS v6

### 3a. Study Workspace (chọn deck)
- List private decks với stats (learning, review, new)
- View toggle: Card | Table
- Click deck → `/flashcard/study/session/{deckId}`

### 3b. Study Session (phiên học)
1. **Loading:** "Loading session..."
2. **No cards due:** "Session complete. No due cards right now."
3. **Card not revealed:** "Tap the card to reveal answer and rating buttons."
4. **Card revealed:** 4 rating buttons (Again/Hard/Good/Easy) + interval preview under each button
5. **After rate:** Pop card từ danh sách, show next card hoặc auto-reload trong 15s

**Interval formatter:**
- 0 days → "Today"
- < 1 day → "Xm" (minutes)
- 1-29 days → "Xd"
- >= 30 days → "Xmon"

### API
```
GET /api/v1/study-session/decks/{deckId}/next-cards?size=20
GET /api/v1/study-session/cards/{cardId}/preview-ratings
POST /api/v1/study-session/cards/{cardId}/rate { rating, timeSpentMs }
```

---

## 4. Folder Structure

```
frontend/src/
├─ features/
│  ├─ decks/DecksPage.tsx
│  ├─ cards-workspace/CardsWorkspacePage.tsx
│  ├─ study-workspace/StudyWorkspacePage.tsx
│  └─ study-session/StudySessionPage.tsx
├─ services/
│  ├─ privateWorkspaceApi.ts
│  ├─ studySessionApi.ts
│  └─ authSession.ts
└─ router/AppRouter.tsx
```

---

## 5. Token Management

- `access_token`: memory/sessionStorage
- `refresh_token`: HttpOnly cookie (auto gửi)
- Auto refresh khi expired
- Logout: clear + API call

