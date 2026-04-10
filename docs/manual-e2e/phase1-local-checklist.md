# Phase 1 Manual E2E Checklist (Local)

Date: 2026-04-10
Scope: TASK-1-TEST-005
Environment:
- Backend: `http://localhost:8080`
- Frontend: `http://localhost:5173`
- Test user: `khaleo@khaleo.app / khaleo`

## Pre-check
- [ ] Docker daemon is running
- [ ] MySQL container is up (`docker compose up -d` at repo root)
- [ ] Backend starts without migration errors
- [ ] Frontend starts without compile errors
- [ ] Browser DevTools Console has no startup errors

## Flow 1 - Login, Deck, Card CRUD
- [ ] Open `/login`
- [ ] Login with seeded user
- [ ] Redirect to Decks page
- [ ] Create a new deck (name + description)
- [ ] Navigate to Cards tab
- [ ] Add at least 1 card into created deck
- [ ] Search card by front/back text and verify result appears

## Flow 2 - Study Session
- [ ] Navigate to Study page
- [ ] Start session for created deck
- [ ] Reveal card answer
- [ ] Rate card with `Again` then `Good`
- [ ] Verify next review value changes after rating
- [ ] Refresh page and verify study progress persists

## Flow 3 - Auth, Refresh, Error Handling
- [ ] Logout redirects to `/login`
- [ ] Access protected route while logged out redirects to `/login`
- [ ] Keep app idle ~15 minutes, trigger API request, verify token refresh works
- [ ] Trigger invalid card payload and verify HTTP 400 validation response
- [ ] Verify CORS is not blocked for frontend -> backend requests

## Evidence Collection
- [ ] Screenshot: successful login
- [ ] Screenshot: created deck + created card
- [ ] Screenshot: study screen with rating buttons
- [ ] Screenshot: logout redirect
- [ ] Screenshot: Network tab showing refresh flow
- [ ] Attach command output from `docs/manual-e2e/phase1-local-e2e-results.md`

