# SPEC - Frontend + Backend Integration

## Feature Overview

KhaLeo là web app học flashcard dùng FSRS v6. 3 tab chính: Decks, Cards, Study.

## Acceptance Criteria

- [ ] Frontend (React) chạy trên localhost:5173 (Vite dev)
- [ ] Backend (Spring Boot) chạy trên localhost:8080
- [ ] Frontend gọi Backend API thành công (CORS setup đúng)
- [ ] JWT auth workflow: login → access token + refresh token
- [ ] Auto refresh token khi expired
- [ ] 3 tabs (Decks/Cards/Study) hoạt động CRUD + FSRS logic
- [ ] Error handling: 401 (relogin), 403 (forbidden), 4xx (validate), 5xx (server error)
- [ ] Empty states + loading states
- [ ] Logout xóa token + redirect login

---

## User Journey

### 1. Unauthorized User

```
User vào localhost:5173
  ↓
Redirect /login
  ↓
Form: identifier(username hoac email) + password
  ↓
POST /api/v1/auth/login { identifier, password }
  ↓
Backend tra: { accessToken, refreshToken, expiresIn }
  ↓
Frontend lưu session auth trong localStorage (`accessToken`, `refreshToken`, `currentUser`)
  ↓
Redirect /flashcard/decks (home)
```

### 2. Authorized User - Decks Tab

```
GET /api/v1/private/decks?q={search}&page=0&size=50
  + Authorization: Bearer {accessToken}
  ↓
Backend validate JWT, return deck list
  ↓
Frontend show card view / table view
  ↓
User action:
  - Click deck → load stats + cards
  - Create deck: POST /api/v1/private/decks
  - Edit deck: PUT /api/v1/private/decks/{id}
  - Delete deck: DELETE /api/v1/private/decks/{id}
  - Start Study: navigate to /flashcard/study/session/{deckId}
```

### 3. Cards Tab

```
Left Panel: List decks (user chọn deck)
  ↓
Right Panel: Load cards của deck
  ↓
GET /api/v1/private/decks/{deckId}/cards/search?frontText={q}&page={p}&size={sz}
  ↓
Show table pagination
  ↓
User action:
  - Create card: POST /api/v1/decks/{deckId}/cards
  - Update card: PUT /api/v1/cards/{cardId}
  - Delete card: DELETE /api/v1/cards/{cardId}
```

### 4. Study Tab

#### 4a. Study Workspace (Select Deck)
```
GET /api/v1/private/decks
  + Parallel: GET /api/v1/private/decks/{id}/stats (per deck)
  ↓
Show decks + stats (learning, review, new_cards)
  ↓
User click "Start session" → navigate to session page
```

#### 4b. Study Session (Learn)
```
Load session data:
  GET /api/v1/study-session/decks/{deckId}/next-cards?size=20
  ↓
Backend return: [card1, card2, ...] danh sách thẻ due hôm nay
  ↓
Frontend loop:
  Show card (front side, click to reveal)
  ↓
User click reveal → show back
  ↓
Show 4 buttons: Again, Hard, Good, Easy
  ↓
User click button (vd: Good)
  ↓
Frontend calculate elapsed time (ms) since card shown
  ↓
POST /api/v1/study-session/cards/{cardId}/rate
    { rating: "GOOD", timeSpentMs: 5000 }
  ↓
Backend apply FSRS v6 logic, return:
    { state, nextReviewAt, scheduledDays, newStability, newDifficulty }
  ↓
Frontend animate card fade out
  ↓
Load next card → repeat
  ↓
No more cards → Show "Session complete"
```

---

## API Contract

### Auth Endpoints

#### Login
```
POST /api/v1/auth/login
Content-Type: application/json

Request:
{
  "identifier": "khaleo",
  "password": "khaleo"
}

Success (200):
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "expiresIn": 900
}

Error (401):
{
  "code": "INVALID_CREDENTIALS",
  "message": "Identifier or password is incorrect",
  "timestamp": "2026-04-10T12:00:00Z",
  "path": "/api/v1/auth/login"
}

Error (423): Account locked (brute force)
{
  "code": "ACCOUNT_LOCKED",
  "message": "Account is locked until <timestamp>",
  "timestamp": "2026-04-10T12:00:00Z",
  "path": "/api/v1/auth/login"
}
```

#### Refresh Token
```
POST /api/v1/auth/refresh
Content-Type: application/json

Request:
{
  "refreshToken": "<refresh-token>"
}

Success (200):
{
  "accessToken": "eyJhbGc...",
  "refreshToken": null,
  "expiresIn": 900
}

Error (401):
{
  "code": "INVALID_REFRESH_TOKEN",
  "message": "Refresh token expired or invalid",
  "timestamp": "2026-04-10T12:00:00Z",
  "path": "/api/v1/auth/refresh"
}
```

#### Logout
```
POST /api/v1/auth/logout
Content-Type: application/json
Body: { "refreshToken": "<refresh-token>" }

Success (204): No content

Backend action: revoke refresh token
```

#### Register
```
POST /api/v1/auth/register
Content-Type: application/json

Request:
{
  "username": "khaleo",
  "email": "khaleo@gmail.com", // optional, co the null
  "password": "khaleo"
}

Success (201):
{
  "userId": "uuid",
  "username": "khaleo",
  "email": "khaleo@gmail.com" // co the null neu dang ky khong email
}
```

#### Removed In Current Phase
```
Khong ho tro cac endpoint:
- /api/v1/auth/verify-email
- /api/v1/auth/forgot-password
- /api/v1/auth/reset-password
```

### Deck Endpoints

```
GET /api/v1/private/decks?q={search}&page=0&size=50
  Authorization: Bearer {token}
  Response: { content: [{id, name, description, isPublic}], totalPages, page, size }

POST /api/v1/private/decks
  Body: { name, description }
  Response: 201 { id, name, description, ... }

PUT /api/v1/private/decks/{id}
  Body: { name, description }
  Response: 200 { id, name, description, ... }

DELETE /api/v1/private/decks/{id}
  Response: 204

GET /api/v1/private/decks/{id}/stats
  Response: { deckId, learning, review, new_cards }
```

### Card Endpoints

```
GET /api/v1/private/decks/{deckId}/cards/search?frontText={q}&backText={q}&page={p}&size={sz}
  Response: { content: [{id, deckId, term, answer, version}], ... }

POST /api/v1/decks/{deckId}/cards
  Body: { term, answer }
  Response: 201 { id, deckId, term, answer, ... }

PUT /api/v1/cards/{cardId}
  Body: { term, answer, version }
  Response: 200 { id, term, answer, ... }

DELETE /api/v1/cards/{cardId}
  Response: 204
```

### Study Session Endpoints

```
GET /api/v1/study-session/decks/{deckId}/next-cards?size=20
  Authorization: Bearer {token}
  Response: {
    items: [
      {
        cardId, deckId, frontText, backText, state,
        nextReviewDate, sourceTier, ...
      }
    ],
    hasMore: boolean,
    nextContinuationToken: string | null
  }

GET /api/v1/study-session/cards/{cardId}/preview-ratings
  Authorization: Bearer {token}
  Response: {
    again: { nextReviewAt, scheduledDays, nextState },
    hard: { nextReviewAt, scheduledDays, nextState },
    good: { nextReviewAt, scheduledDays, nextState },
    easy: { nextReviewAt, scheduledDays, nextState }
  }

POST /api/v1/study-session/cards/{cardId}/rate
  Authorization: Bearer {token}
  Body: { rating: "AGAIN"|"HARD"|"GOOD"|"EASY", timeSpentMs: number }
  Response: 200 {
    cardId, state, nextReviewAt, scheduledDays,
    newStability, newDifficulty
  }
```

---

## Frontend State Management

### Auth State
```typescript
interface AuthState {
  currentUser: { id, username, email, role } | null
  accessToken: string | null
  isAuthenticated: boolean
  loading: boolean
  error: string | null
}

Methods:
- login(identifier, password)
- register(username, email | null, password)
- logout()
- bootstrap()
- setCurrentUser(user)
```

### Deck State (Decks Tab)
```typescript
interface DeckState {
  decks: Deck[]
  selectedDeck: Deck | null
  stats: Record<string, DeckStats>
  searchInput: string
  loading: boolean
  error: string | null
}
```

### Cards State (Cards Tab)
```typescript
interface CardsState {
  decks: Deck[]
  selectedDeckId: string | null
  cards: Card[]
  currentPage: number
  totalPages: number
  pageSize: number
  searchTerm: string
  loading: boolean
  error: string | null
}
```

### Study State (Study Tab)
```typescript
interface StudyState {
  // Workspace
  decks: Deck[]
  deckStats: Record<string, DeckStats>
  selectedViewMode: 'card' | 'table'
  
  // Session
  deckId: string | null
  cards: Card[]
  currentCard: Card | null
  cardRevealed: boolean
  ratingPreview: RatingPreviews | null
  shownAt: timestamp
  loading: boolean
  error: string | null
}
```

---

## Error Handling

### Status Codes
| Status | Action | Frontend |
|--------|--------|----------|
| 200-204 | Success | Store data / redirect |
| 400 | Validation | Show error toast + highlight fields |
| 401 | Unauthorized | Clear token + redirect /login |
| 403 | Forbidden | Show "Access denied" message |
| 404 | Not found | Show empty state |
| 409 | Conflict | Optimistic locking (version mismatch) retry |
| 423 | Account locked | Show lockout message, cho user doi het thoi gian khoa |
| 500 | Server error | Show "Server error. Try again later" |

### Error Response Format
```json
{
  "code": "VALIDATION_ERROR",
  "message": "Deck name is required",
  "timestamp": "2026-04-10T12:00:00Z",
  "path": "/api/v1/private/decks"
}
```

### Frontend Handling
```typescript
function handleApiError(error) {
  if (error.status === 401) {
    // Clear token, redirect to login
    clearAuth();
    navigate('/login');
  } else if (error.status === 403) {
    showError('Access denied');
  } else if (error.status === 400) {
    // Show validation errors
    showError(error.response.message);
  } else if (error.status >= 500) {
    showError('Server error. Please try again later');
  }
}
```

---

## CORS Configuration

### Backend (Spring Boot)
```java
@Configuration
public class CorsConfig {
  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
          .allowedOrigins("http://localhost:5173", "http://localhost:3000")  // dev
          .allowedOrigins("https://staging.khaleoshop.click")  // staging
          .allowedOrigins("https://khaleoshop.click")  // prod
          .allowedMethods("GET", "POST", "PUT", "DELETE")
          .allowedHeaders("*")
          .allowCredentials(true)
          .maxAge(3600);
      }
    };
  }
}
```

### Frontend (React)
```typescript
// Vite config: vite.config.ts
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api')
      }
    }
  }
})

// Or use environment variables
const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
```

---

## Token Lifecycle

### Access Token
- TTL: 15 minutes
- Stored: localStorage trong `khaleo-auth-session`
- Sent: Authorization: Bearer {token}
- Lifecycle:
  - Backend issues on login
  - Frontend luu vao session object
  - Khi app khoi dong lai, frontend bootstrap lai auth state tu localStorage

### Refresh Token
- TTL: 7 days
- Stored: localStorage trong `khaleo-auth-session`
- Sent: JSON body cho `/api/v1/auth/refresh` va `/api/v1/auth/logout`
- Lifecycle:
  - Backend issues on login
  - Frontend luu kem access token
  - Khi API tra 401, frontend thu refresh roi retry 1 lan
  - Backend giu nguyen refresh token (khong rotate), chi cap access token moi

### Refresh Strategy
```typescript
// Frontend: requestJson() trong apiClient retry 1 lan khi gap 401
async function withTokenRefresh(apiCall) {
  try {
    return await apiCall();
  } catch (error) {
    if (error.status === 401 && canRetry) {
      // Try refresh bang refreshToken trong localStorage session
      await refreshAccessToken(storedRefreshToken);
      // Retry original call
      return await apiCall();
    }
    throw error;
  }
}
```

---

## Loading & Empty States

### Loading States
```typescript
{loading && <Spinner />}
{!loading && data.length === 0 && <EmptyState />}
{!loading && data.length > 0 && <Content data={data} />}
```

### Empty States Messages
- **Decks:** "No decks yet. Create your first!"
- **Cards (deck not selected):** "Select a deck to view cards"
- **Cards (no cards in deck):** "No cards in this deck. Create your first!"
- **Study (no cards due):** "Session complete. No due cards right now."

---

## Validation Rules

### Deck
- Name: required, min 1 char, max 255
- Description: optional, max 1000

### Card
- Term: required, min 1 char, max 500
- Answer: required, min 1 char, max 5000

### Login
- Identifier: required (username hoac email)
- Password: required, min 8 chars

### JWT Claims (access token)
- Required: `sub`, `role`, `username`
- Transitional/backward-compatibility: `email` (chi co khi user co email)

---

## Testing Scenarios

### Happy Path
1. Register → Login → See decks → Create deck → Add card → Study card → Rate card → See stats

### Error Cases
1. Login with wrong password → 401 error message
2. Create deck without name → 400 validation error
3. Refresh token expired → 401, redirect login
4. Network error → Show retry toast
5. Optimistic lock conflict (version mismatch) → Retry with new version

### Edge Cases
1. Rapid API calls → debounce/throttle
2. Page refresh during study → Restore state từ URL
3. Logout → Clear auth state + localStorage
4. Multiple tabs open → Sync auth state (storage listener)

