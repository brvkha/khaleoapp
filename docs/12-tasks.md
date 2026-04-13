# TASKS - Actionable Development Tasks (Dependency-Ordered)

## Overview

Tập hợp các tasks chi tiết, dependency-ordered, cho Phase 1-4 của dự án KhaLeo.

Format: `TASK-{Phase}{Layer}{Number}`
- Status: TODO, IN_PROGRESS, DONE
- Depends: Reference các task dependencies
- Owner: Role (Backend Dev, Frontend Dev, DevOps, QA)
- Est: Effort estimate (hrs)

---

## Phase 1: Local Development (Week 1)

### Layer 1.0: Infrastructure Setup

**TASK-1-INF-001: Setup Development Environment**
- Status: DONE
- Depends: -
- Owner: DevOps
- Est: 2h
- Description:
  - Create project folders: backend/, frontend/, infra/
  - Git repo init + .gitignore
  - Create docker-compose.yml (MySQL 8.0, no volume for ephemeral)
  - Create README.md with dev setup instructions

**TASK-1-INF-002: Spring Boot Project Init**
- Status: DONE
- Depends: TASK-1-INF-001
- Owner: Backend Dev
- Est: 1h
- Description:
  - Create Spring Boot 3.x project (JDK 17)
  - Maven pom.xml: spring-web, spring-data-jpa, spring-security, mysql-connector, lombok, jwt libs
  - Basic project structure: src/main/java/com/khaleo/flashcard/{controller,service,entity,repository,config}
  - application.yml: datasource, jpa, server.port=8080

**TASK-1-INF-003: React + Vite Project Init**
- Status: DONE
- Depends: TASK-1-INF-001
- Owner: Frontend Dev
- Est: 1h
- Description:
  - Create React 18 + TypeScript project via Vite
  - npm install: tailwind, react-router, zustand
  - vite.config.ts: proxy /api to localhost:8080
  - tailwind.config.js, postcss.config.js
  - Folder structure: src/{features,services,components,store,router}

**TASK-1-INF-004: MySQL Database Init (Local)**
- Status: DONE
- Depends: TASK-1-INF-001
- Owner: Backend Dev
- Est: 0.5h
- Description:
  - docker-compose up -d
  - Verify MySQL running on 3306
  - Create initial user: root / (no password in local)

---

### Layer 1.1: Auth Backend (JWT + Spring Security)

**TASK-1-AUTH-001: User Entity + Repository**
- Status: DONE
- Depends: TASK-1-INF-002
- Owner: Backend Dev
- Est: 1h
- Description:
  - Create User entity (id, email, password_hash, role, verified, banned, created_at, updated_at)
  - UserRepository: findByEmail, findById
  - Lombok annotations: @Getter, @Setter, @Entity, @Table

**TASK-1-AUTH-002: RefreshToken Entity + Repository**
- Status: DONE
- Depends: TASK-1-AUTH-001
- Owner: Backend Dev
- Est: 0.5h
- Description:
  - Create RefreshToken entity (id, user_id, token_hash, expires_at, revoked, created_at)
  - RefreshTokenRepository: findByUserIdAndNotRevoked, findByTokenHash

**TASK-1-AUTH-003: LoginAttempt Entity + Repository**
- Status: DONE
- Depends: TASK-1-AUTH-001
- Owner: Backend Dev
- Est: 0.5h
- Description:
  - Create LoginAttempt entity (id, email, success, ip_address, user_agent, attempted_at)
  - LoginAttemptRepository: countRecentFailedAttempts(email)

**TASK-1-AUTH-004: JWT Token Service**
- Status: DONE
- Depends: TASK-1-AUTH-002
- Owner: Backend Dev
- Est: 2h
- Description:
  - JwtTokenService: generateAccessToken(user), generateRefreshToken(user), validateToken(token), parseToken(token)
  - Access token TTL: 15 min, Refresh token TTL: 7 days
  - Sign with JWT secret (from config)
  - Include: user id, email, role in claims

**TASK-1-AUTH-005: Auth Service (Login/Logout/Refresh)**
- Status: DONE
- Depends: TASK-1-AUTH-004
- Owner: Backend Dev
- Est: 2h
- Description:
  - AuthenticationService: login(email, password), logout(user_id, refresh_token), refreshAccessToken(refresh_token)
  - Password hashing: BCryptPasswordEncoder
  - Rotation: invalidate old refresh token, issue new one
  - Response: { accessToken, refreshToken, user }

**TASK-1-AUTH-006: LoginLockout Service**
- Status: DONE
- Depends: TASK-1-AUTH-003
- Owner: Backend Dev
- Est: 1h
- Description:
  - LoginLockoutService: recordFailedAttempt(email), isLockedOut(email), clearAttempts(email)
  - Logic: 5 failed attempts → lock 15 min
  - Query: failed attempts in last 15 min

**TASK-1-AUTH-007: JWT Auth Filter**
- Status: DONE
- Depends: TASK-1-AUTH-004
- Owner: Backend Dev
- Est: 1h
- Description:
  - JwtAuthenticationFilter: extract Bearer token from header, validate, set SecurityContext
  - On invalid/expired: return 401
  - Allow /auth/login, /auth/refresh without token

**TASK-1-AUTH-008: Spring Security Config**
- Status: DONE
- Depends: TASK-1-AUTH-007
- Owner: Backend Dev
- Est: 1h
- Description:
  - SecurityConfig: configure filter chain, CORS, auth endpoints
  - CORS: Allow localhost:5173, credentials=true
  - Auth endpoints: /api/v1/auth/** no authentication required
  - Other endpoints: require authentication
  - Customize error responses (401, 403)

**TASK-1-AUTH-009: Auth Controller (Login/Logout/Refresh)**
- Status: DONE
- Depends: TASK-1-AUTH-008
- Owner: Backend Dev
- Est: 1h
- Description:
  - AuthController: @PostMapping /login, /logout, /refresh
  - DTOs: LoginRequest, LoginResponse, RefreshTokenRequest, LogoutRequest
  - Error handling: 401 (credentials), 429 (locked out)
  - Success: 200 with tokens

---

### Layer 1.2: Deck CRUD Backend



**TASK-1-DECK-001: Deck Entity + Repository**
- Status: DONE
- Depends: TASK-1-INF-002
- Owner: Backend Dev
- Est: 0.5h
- Description:
  - Deck entity (id, user_id, name, description, is_public, created_at, updated_at)
  - DeckRepository: findByUserId, searchByName, pagination

**TASK-1-DECK-002: DeckCrudService**
- Status: DONE
- Depends: TASK-1-DECK-001, TASK-1-AUTH-001
- Owner: Backend Dev
- Est: 1h
- Description:
  - PrivateDeckCrudService: createDeck(user, name, desc), readDeck(id, user), updateDeck, deleteDeck
  - Authorization: only deck owner can modify
  - Validation: name required (1-255), desc optional (0-1000)

**TASK-1-DECK-003: DeckStatsService**
- Status: DONE
- Depends: TASK-1-DECK-002
- Owner: Backend Dev
- Est: 1h
- Description:
  - DeckStatsService: getDeckStats(deckId, userId) → { learning, review, new_cards }
  - Queries: count CardLearningState by state, deck

**TASK-1-DECK-004: PrivateWorkspaceController**
- Status: DONE
- Depends: TASK-1-DECK-003
- Owner: Backend Dev
- Est: 1h
- Description:
  - PrivateWorkspaceController: GET/POST/PUT/DELETE /api/v1/private/decks
  - DTOs: DeckResponse, CreateDeckRequest, UpdateDeckRequest
  - Pagination: page, size defaults
  - Authorization: require authentication

---

### Layer 1.3: Card CRUD Backend

**TASK-1-CARD-001: Card Entity + Repository**
- Status: DONE
- Depends: TASK-1-INF-002
- Owner: Backend Dev
- Est: 0.5h
- Description:
  - Card entity (id, deck_id, term, answer, version, created_at, updated_at)
  - CardRepository: findByDeckId, search (term/answer), pagination

**TASK-1-CARD-002: CardCrudService**
- Status: DONE
- Depends: TASK-1-CARD-001, TASK-1-DECK-002
- Owner: Backend Dev
- Est: 1h
- Description:
  - CardService: createCard(deck, term, answer), readCard, updateCard (with version), deleteCard
  - Validation: term (1-500), answer (1-5000)
  - Optimistic locking: check version on update

**TASK-1-CARD-003: CardController**
- Status: DONE
- Depends: TASK-1-CARD-002
- Owner: Backend Dev
- Est: 1h
- Description:
  - CardController: GET/POST/PUT/DELETE /api/v1/decks/{id}/cards
  - Search endpoint: GET /api/v1/private/decks/{id}/cards/search?frontText={q}&backText={q}&page={p}
  - DTOs: CardResponse, CreateCardRequest, UpdateCardRequest

---

### Layer 1.4: FSRS v6 Engine

**TASK-1-FSRS-001: CardLearningState Entity + Repository**
- Status: DONE
- Depends: TASK-1-INF-002
- Owner: Backend Dev
- Est: 1h
- Description:
  - CardLearningState entity (state, stability, difficulty, reps, lapses, learning_step_good_count, last_reviewed_at, next_review_at, version)
  - Enums: CardLearningStateType (NEW, LEARNING, REVIEW, RELEARNING, MASTERED)
  - CardLearningStateRepository: findByUserIdAndCardId, queries for due cards
  - Index: (user_id, next_review_at, state)

**TASK-1-FSRS-002: SpacedRepetitionService (Core FSRS v6)**
- Status: DONE
- Depends: TASK-1-FSRS-001
- Owner: Backend Dev
- Est: 4h
- Description:
  - SpacedRepetitionService: apply(state, rating, now) → RatingOutcome
  - Implement all w0-w18 parameters
  - Formulas: init stability/difficulty, next difficulty, recall/forget stability, same-day handling
  - Handle states: NEW, LEARNING, REVIEW, RELEARNING
  - Calculate next review date + interval
  - Return: RatingOutcome(state, nextReviewAt, scheduledDays, stability, difficulty, reps, lapses)

**TASK-1-FSRS-003: StudySchedulerService**
- Status: DONE
- Depends: TASK-1-FSRS-002
- Owner: Backend Dev
- Est: 1h
- Description:
  - Wrapper around SpacedRepetitionService
  - schedulerService.apply(state, rating, now) delegates to spacedRepetitionService

**TASK-1-FSRS-004: NextCardsService**
- Status: DONE
- Depends: TASK-1-FSRS-001, TASK-1-CARD-001
- Owner: Backend Dev
- Est: 2h
- Description:
  - NextCardsService: getNextCards(deckId, userId, size=20) → NextCardsPageResponse
  - Query due LEARNING/RELEARNING cards (order by next_review_at)
  - Query due REVIEW/MASTERED cards
  - Query new unseenCards (limited by daily quota)
  - Return: ordered list [learning + review + new], pagination token

**TASK-1-FSRS-005: StudyRatingService**
- Status: DONE
- Depends: TASK-1-FSRS-004
- Owner: Backend Dev
- Est: 2h
- Description:
  - StudyRatingService: rateCard(cardId, userId, rating, timeSpentMs) → RateCardResponse
  - Load or create CardLearningState
  - Apply FSRS logic via scheduler
  - Update state, next_review_at, stability, difficulty, reps, lapses, learning_step_good_count
  - Persist with optimistic locking retry
  - Return: RateCardResponse

**TASK-1-FSRS-006: StudySessionController**
- Status: DONE
- Depends: TASK-1-FSRS-005
- Owner: Backend Dev
- Est: 1h
- Description:
  - StudySessionController: GET /api/v1/study-session/decks/{id}/next-cards
  - GET /api/v1/study-session/cards/{id}/preview-ratings
  - POST /api/v1/study-session/cards/{id}/rate
  - DTOs: NextCardsPageResponse, RateCardRequest, RateCardResponse

---

### Layer 1.5: Database & Migrations

**TASK-1-DB-001: Flyway Migration V1 (Schema)**
- Status: DONE
- Depends: TASK-1-INF-004
- Owner: Backend Dev
- Est: 1h
- Description:
  - Create V1__init_tables.sql at src/main/resources/db/migration/
  - Tables: users, decks, cards, card_learning_states, refresh_tokens, login_attempts
  - Indexes: (user_id), (deck_id), (next_review_at, state), (email, attempted_at)
  - Constraints: FK, UNIQUE, NOT NULL

**TASK-1-DB-002: Flyway Seed Data**
- Status: DONE
- Depends: TASK-1-DB-001
- Owner: Backend Dev
- Est: 0.5h
- Description:
  - Create V2__seed_test_data.sql
  - Seed: 1 test user (email: user@test.com, password: hashed "password123")
  - No decks/cards in seed (user creates via UI)

**TASK-1-DB-003: Test DB Connection**
- Status: DONE
- Depends: TASK-1-DB-002
- Owner: Backend Dev
- Est: 0.5h
- Description:
  - Run Spring Boot app, verify migrations auto-run
  - Check tables created in MySQL
  - Seed data inserted

---

### Layer 1.6: Frontend Auth

**TASK-1-FE-AUTH-001: Auth Store (Zustand)**
- Status: DONE
- Depends: TASK-1-INF-003
- Owner: Frontend Dev
- Est: 1h
- Description:
  - AuthStore: currentUser, accessToken, isAuthenticated, loading, error
  - Methods: login(email, pass), logout(), refreshToken(), setCurrentUser()
  - Access token: store in memory
  - Refresh token: auto via httpOnly cookie (browser handles)

**TASK-1-FE-AUTH-002: Auth API Client**
- Status: DONE
- Depends: TASK-1-FE-AUTH-001
- Owner: Frontend Dev
- Est: 1h
- Description:
  - authApi.ts: login(email, pass), logout(), refreshAccessToken()
  - API base: http://localhost:8080/api/v1
  - Error handling: 401 → clear auth + redirect login

**TASK-1-FE-AUTH-003: Auth Guard**
- Status: DONE
- Depends: TASK-1-FE-AUTH-002
- Owner: Frontend Dev
- Est: 0.5h
- Description:
  - RequireAuth component: check isAuthenticated, redirect /login if not
  - Route protection in router

**TASK-1-FE-AUTH-004: Login Page**
- Status: DONE
- Depends: TASK-1-FE-AUTH-003
- Owner: Frontend Dev
- Est: 1.5h
- Description:
  - LoginPage.tsx: form (email, password), submit → login()
  - Loading state, error message display
  - On success: navigate /flashcard/decks
  - Tailwind styling

**TASK-1-FE-AUTH-005: Token Refresh Interceptor**
- Status: DONE
- Depends: TASK-1-FE-AUTH-004
- Owner: Frontend Dev
- Est: 1h
- Description:
  - API interceptor: check 401 response → call refreshAccessToken() → retry
  - Update access token in memory
  - Proactive refresh: 1 min before expiry

---

### Layer 1.7: Frontend Layout & Navigation

**TASK-1-FE-LAYOUT-001: App Router**
- Status: DONE
- Depends: TASK-1-FE-AUTH-005
- Owner: Frontend Dev
- Est: 1h
- Description:
  - AppRouter.tsx: Route structure
  - /login, /register (future)
  - /flashcard/{decks, cards, study}
  - /admin (future)
  - Route guards

**TASK-1-FE-LAYOUT-002: Layout Component**
- Status: DONE
- Depends: TASK-1-FE-LAYOUT-001
- Owner: Frontend Dev
- Est: 1h
- Description:
  - Layout.tsx: header (logo, tabs, user, logout), sidebar, main, notification center
  - Tabs: Decks | Cards | Study | (Admin if role=ADMIN)
  - Responsive: mobile hide sidebar, show mobile nav

**TASK-1-FE-LAYOUT-003: Notification Store**
- Status: DONE
- Depends: TASK-1-FE-LAYOUT-002
- Owner: Frontend Dev
- Est: 0.5h
- Description:
  - NotificationStore (Zustand): toast/snackbar state
  - Methods: showSuccess, showError, showWarning
  - Auto-dismiss after 3s

---

### Layer 1.8: Frontend Decks Tab

**TASK-1-FE-DECKS-001: Decks API Client**
- Status: DONE
- Depends: TASK-1-FE-AUTH-002
- Owner: Frontend Dev
- Est: 0.5h
- Description:
  - privateWorkspaceApi.ts: listDecks(query), createDeck, updateDeck, deleteDeck, getDeckStats
  - Response types: DeckDto, DeckStatsDto

**TASK-1-FE-DECKS-002: Decks Store**
- Status: DONE
- Depends: TASK-1-FE-DECKS-001
- Owner: Frontend Dev
- Est: 1h
- Description:
  - Store: decks[], selectedView, searchInput, loading, error
  - Methods: loadDecks(), createDeck(), updateDeck(), deleteDeck()

**TASK-1-FE-DECKS-003: DecksPage Component**
- Status: DONE
- Depends: TASK-1-FE-DECKS-002
- Owner: Frontend Dev
- Est: 2h
- Description:
  - DecksPage.tsx: search input, view toggle (Card/Table), create button
  - Card view: grid, show name/desc/stats, buttons (Start Study, Edit, Delete)
  - Table view: columns (Name, Desc, Learning, Review, New, Action)
  - Empty state: "No decks yet..."
  - Modals: create, edit
  - Tailwind styling

---

### Layer 1.9: Frontend Cards Tab

**TASK-1-FE-CARDS-001: Cards API Client**
- Status: DONE
- Depends: TASK-1-FE-AUTH-002
- Owner: Frontend Dev
- Est: 0.5h
- Description:
  - privateWorkspaceApi.ts: searchCards(deckId, query, page, size), createCard, updateCard, deleteCard
  - Response types: CardDto, CardSearchPageDto

**TASK-1-FE-CARDS-002: Cards Store**
- Status: DONE
- Depends: TASK-1-FE-CARDS-001
- Owner: Frontend Dev
- Est: 1h
- Description:
  - Store: decks[], selectedDeckId, cards[], currentPage, totalPages, searchTerm, loading, error
  - Methods: loadDecks(), selectDeck(), loadCards(), createCard(), updateCard(), deleteCard()

**TASK-1-FE-CARDS-003: CardsWorkspacePage Component**
- Status: DONE
- Depends: TASK-1-FE-CARDS-002
- Owner: Frontend Dev
- Est: 2.5h
- Description:
  - CardsWorkspacePage.tsx: 2-panel layout
  - Left: deck list (click to select), "+ Create Deck" button
  - Right: search bar, "+ Add Card" button, card table (term, answer, action)
  - Inline edit: click Update → inputs appear
  - Pagination: page size selector, Prev/Next
  - Empty states: "Select a deck...", "No cards..."
  - Modals: create deck, create card
  - Tailwind styling

---

### Layer 1.10: Frontend Study Tab

**TASK-1-FE-STUDY-001: Study Session API Client**
- Status: DONE
- Depends: TASK-1-FE-AUTH-002
- Owner: Frontend Dev
- Est: 0.5h
- Description:
  - studySessionApi.ts: getNextSessionCards(deckId), previewRatings(cardId), rateCard(cardId, rating, timeMs)
  - Response types: StudySessionCardDto, RatingPreviewDto, RateCardResponseDto

**TASK-1-FE-STUDY-002: Study Workspace Page**
- Status: DONE
- Depends: TASK-1-FE-STUDY-001
- Owner: Frontend Dev
- Est: 1.5h
- Description:
  - StudyWorkspacePage.tsx: list decks + stats
  - View toggle: Card | Table
  - Card view: grid, show deck name, stats (Learning, Review, New), "Start session" button
  - Table view: columns (Name, Description, Learning, Review, New, Action)
  - Search/debounce
  - Load deck stats in parallel
  - Empty state: "No decks yet..."

**TASK-1-FE-STUDY-003: Study Session Page**
- Status: DONE
- Depends: TASK-1-FE-STUDY-002
- Owner: Frontend Dev
- Est: 3h
- Description:
  - StudySessionPage.tsx: main study UI
  - Load cards on mount: GET /api/v1/study-session/decks/{deckId}/next-cards
  - Display current card (front side)
  - Click reveal → show back side
  - 4 rating buttons (Again, Hard, Good, Easy) appear when revealed
  - Below each button: preview interval (Today, 10m, 3d, ...)
  - On click: POST rate → backend applies FSRS → pop card → load next
  - "Session complete" when no cards
  - Auto-reload every 15s if no more cards
  - Track time spent per card
  - Tailwind styling + animations

---

### Layer 1.11: Testing Phase 1

**TASK-1-TEST-001: Backend Unit Tests (FSRS)**
- Status: DONE
- Depends: TASK-1-FSRS-002
- Owner: Backend Dev
- Est: 2h
- Description:
  - SpacedRepetitionServiceTest: test each FSRS path (NEW→LEARNING, LEARNING→REVIEW, REVIEW repeat, Again→RELEARNING)
  - Verify stability/difficulty calculations
  - Verify next review date calculation
  - Test all 4 ratings

**TASK-1-TEST-002: Backend Unit Tests (Services)**
- Status: DONE
- Depends: TASK-1-DECK-002, TASK-1-CARD-002, TASK-1-FSRS-005
- Owner: Backend Dev
- Est: 2h
- Description:
  - DeckServiceTest, CardServiceTest, StudyRatingServiceTest
  - Mock repositories
  - Test authorization checks
  - Test validation

**TASK-1-TEST-003: Backend Integration Tests (Swagger/OpenAPI)**
- Status: DONE
- Depends: TASK-1-AUTH-009, TASK-1-DECK-004, TASK-1-CARD-003, TASK-1-FSRS-006
- Owner: QA
- Est: 2h
- Description:
  - Integration test with Spring Boot + MockMvc (khong dung Postman collection)
  - Verify `GET /swagger-ui/index.html` hoat dong
  - Verify `GET /v3/api-docs` tra OpenAPI JSON hop le
  - Verify OpenAPI docs co nhom endpoint chinh: auth/deck/card/study

**TASK-1-TEST-004: Frontend Component Tests**
- Status: DONE
- Depends: TASK-1-FE-STUDY-003
- Owner: Frontend Dev
- Est: 2h
- Description:
  - Vitest/React Testing Library
  - Test LoginPage: render form, submit → auth store called
  - Test DecksPage: render list, create/edit/delete
  - Test CardsPage: 2-panel, select deck, search
  - Test StudySessionPage: render card, reveal, rate buttons

**TASK-1-TEST-005: Manual E2E Test (Dev Environment)**
- Status: DONE
- Depends: TASK-1-TEST-004
- Owner: QA + Dev
- Est: 3h
- Description:
  - Setup: Spring Boot localhost:8080, React localhost:5173
  - Flows:
    1. Login → Decks page → Create deck → Cards page → Add card
    2. Study page → Select deck → Start session → Reveal → Rate card → Check next review date
    3. Logout → redirects to login
  - Check CORS working
  - Check token refresh (wait 15 min, trigger refresh)
  - Check error handling (401, 400 validation, etc.)
  - Console errors: none

---

## Phase 2: Staging Deployment (Week 2)

### Layer 2.1: AWS Infrastructure (Terraform)

**TASK-2-INFRA-001: Bootstrap Backend (S3 + DynamoDB)**
- Status: [X] DONE
- Depends: TASK-1-TEST-005 ✅
- Owner: DevOps
- Est: 2h
- Description:
  - Terraform module: bootstrap/main.tf
  - S3 bucket: unique name (e.g., khaleo-tf-state-{date})
  - DynamoDB table: khaleo-terraform-lock (for state lock)
  - Outputs: bucket name, table name
  - Test: terraform apply, verify resources created

**TASK-2-INFRA-002: Staging VPC + Network**
- Status: [X] DONE
- Depends: TASK-2-INFRA-001
- Owner: DevOps
- Est: 2h
- Description:
  - Terraform module: app/network.tf
  - VPC (CIDR 10.0.0.0/16)
  - Public subnet (10.0.1.0/24), Private subnet (10.0.2.0/24)
  - IGW, NAT Gateway, route tables
  - Security groups: EC2 (80/443/8080), RDS (3306 from EC2)

**TASK-2-INFRA-003: Staging RDS MySQL**
- Status: [X] DONE
- Depends: TASK-2-INFRA-002
- Owner: DevOps
- Est: 1h
- Description:
  - Terraform: app/rds.tf
  - db.t3.micro, 20GB storage
  - Private subnet placement
  - Backup retention: 7 days
  - Multi-AZ: false (cost saving)
  - DB name: khaleoapp, username: app_user, password: from var

**TASK-2-INFRA-004: Staging EC2**
- Status: [X] DONE
- Depends: TASK-2-INFRA-002
- Owner: DevOps
- Est: 1.5h
- Description:
  - Terraform: app/ec2.tf
  - t3.micro, Amazon Linux 2023
  - IAM role for SSM Session Manager + EC2 CloudWatch
  - User data: install Docker, Nginx
  - Elastic IP
  - Security group: 80, 443, 8080

**TASK-2-INFRA-005: Staging S3 + CloudFront**
- Status: [X] DONE
- Depends: TASK-2-INFRA-002
- Owner: DevOps
- Est: 2h
- Description:
  - Terraform: app/s3.tf, app/cloudfront.tf
  - S3 bucket: khaleo-staging-frontend
  - CloudFront distribution: OAC for S3 access
  - HTTPS via ACM certificate
  - Route 53: staging.khaleoshop.click → CloudFront

**TASK-2-INFRA-006: Staging Route 53 + Domains**
- Status: [X] DONE
- Depends: TASK-2-INFRA-005
- Owner: DevOps
- Est: 1h
- Description:
  - Terraform: app/route53.tf
  - Records: staging.khaleoshop.click, stage.khaleoshop.click → CloudFront, api-staging.khaleoshop.click → EC2 EIP

**TASK-2-INFRA-007: Test Terraform Apply (Staging)**
- Status: [X] DONE
- Depends: TASK-2-INFRA-006
- Owner: DevOps
- Est: 1h
- Description:
  - Copy backend-staging.hcl, env/staging.tfvars
  - terraform init, plan, apply
  - Verify all resources created in AWS console
  - Note IPs, RDS endpoint, S3 bucket
  - Evidence: Apply completed 2026-04-10, outputs include `backend_instance_id`, `api_public_ip`, `cloudfront_distribution_id`.

---

### Layer 2.2: GitHub Actions Workflows

**TASK-2-CI-001: Frontend Deploy Workflow**
- Status: [X] DONE
- Depends: TASK-2-INFRA-007
- Owner: DevOps
- Est: 1.5h
- Description:
  - .github/workflows/deploy-frontend.yml
  - Trigger: push develop + frontend/** paths
  - Steps: checkout, node setup, npm ci, build, AWS creds, S3 sync, CloudFront invalidation
  - Test: push to develop, check Actions log, verify S3 has new files

**TASK-2-CI-002: Backend Deploy Workflow**
- Status: [X] DONE
- Depends: TASK-2-INFRA-007
- Owner: DevOps
- Est: 2h
- Description:
  - .github/workflows/deploy-backend.yml
  - Trigger: push develop + backend/** paths
  - Steps: checkout, JDK setup, mvn package, Docker build, push Hub, SSM send-command, docker run
  - Secrets setup in GitHub
  - Test: push to develop, check Actions log

**TASK-2-CI-003: Test FE Deploy (develop branch)**
- Status: [X] DONE
- Depends: TASK-2-CI-001
- Owner: DevOps + Frontend
- Est: 1h
- Description:
  - Create dummy commit on develop + frontend/
  - Push, trigger workflow
  - Check S3 bucket for build files
  - CloudFront invalidation working
  - Evidence: GitHub Actions success run `24232134510`.

**TASK-2-CI-004: Test BE Deploy (develop branch)**
- Status: [X] DONE
- Depends: TASK-2-CI-002
- Owner: DevOps + Backend
- Est: 1h
- Description:
  - Create dummy commit on develop + backend/
  - Push, trigger workflow
  - Verify SSM command success, then check docker ps for running container
  - Test API: curl http://localhost:8080/api/v1/auth/login
  - Evidence: GitHub Actions success run `24235846860` after SSM + disk-capacity fixes.

**TASK-2-CI-005: Terraform Staging Automation Workflow (Bootstrap + Apply/Destroy/Recreate)**
- Status: [X] DONE
- Depends: TASK-2-INFRA-007
- Owner: DevOps
- Est: 1.5h
- Description:
  - Add `.github/workflows/terraform-staging.yml` to automate bootstrap + app stack lifecycle for staging.
  - Support `workflow_dispatch` with `action=apply|destroy|recreate` for destroy/recreate reproducibility.
  - Generate CI tfvars/backend config from staging secrets (no local tfvars dependency).
  - Validate Terraform fmt/validate in workflow before mutating actions.
  - Update runbook/scripts/docs to include new required secrets (`TF_STATE_BUCKET_STAGING`, optional lock/backend key overrides).
  - Added full-auto chain (2026-04-11): successful Terraform `apply`/`recreate` now calls `.github/workflows/deploy-frontend.yml` and `.github/workflows/deploy-backend.yml` via reusable workflow (`workflow_call`) in the same pipeline.
  - Added staging frontend API hardening (2026-04-11): frontend deploy workflow injects `VITE_API_BASE_URL` from `VITE_API_BASE_URL_STAGING` (fallback `https://api-staging.khaleoshop.click`) and fails build validation if `localhost:8080` is still present in built assets.

---

### Layer 2.3: Staging Config

**TASK-2-CONFIG-001: Backend application-staging.yml**
- Status: [X] DONE
- Depends: TASK-2-INFRA-003, TASK-2-INFRA-004
- Owner: Backend Dev
- Est: 0.5h
- Description:
  - Create src/main/resources/application-staging.yml
  - DB_HOST: RDS endpoint (from Terraform output)
  - DB_NAME: khaleoapp, DB_USER: app_user, DB_PASSWORD: from secret
  - Spring profile: staging
  - JWT_SECRET: staging key
  - Log level: info

**TASK-2-CONFIG-002: Frontend .env.staging**
- Status: [X] DONE
- Depends: TASK-2-INFRA-006
- Owner: Frontend Dev
- Est: 0.5h
- Description:
  - Create frontend/.env.staging
  - VITE_API_BASE_URL=https://api-staging.khaleoshop.click

**TASK-2-CONFIG-003: Nginx on EC2 (Reverse Proxy)**
- Status: [X] DONE
- Depends: TASK-2-INFRA-004
- Owner: DevOps
- Est: 1h
- Description:
  - Terraform app module creates SSM document `${project}-${environment}-nginx-tls-bootstrap`
  - Backend deploy workflow executes document before container deployment (SSM only, no SSH key)
  - Config: listen 443 ssl; server_name api-staging.khaleoshop.click; proxy_pass http://127.0.0.1:8080
  - SSL: Let's Encrypt + Certbot with `--keep-until-expiring` for idempotent re-run on every deploy
  - Evidence (2026-04-11 update): manual one-off SSM command replaced by CI/CD-integrated bootstrap in `.github/workflows/deploy-backend.yml` and Terraform resource `aws_ssm_document.nginx_tls_bootstrap`.

---

### Layer 2.4: Staging Validation

**TASK-2-VALIDATE-001: Full Integration Test (Staging)**
- Status: [X] DONE
- Depends: TASK-2-CI-003, TASK-2-CI-004, TASK-2-CONFIG-003
- Owner: QA
- Est: 3h
- Description:
  - Open https://staging.khaleoshop.click in browser
  - Login (user@test.com / password123)
  - Decks tab: create deck
  - Cards tab: add card (term, answer)
  - Study tab: start session, reveal card, rate (all 4 ratings)
  - Check stats update
  - Logout → redirected to login
  - Check token refresh (monitor network tab, wait 14 min 50s, trigger API call)
  - Evidence (2026-04-11): `https://staging.khaleoshop.click` and `https://stage.khaleoshop.click` return `200`; API auth flow validated (`login`, `refresh`, `logout`); EC2 runtime log review via SSM command `7f573019-b999-42c5-9214-81ae963634c5` confirms deck/card create and study ratings `AGAIN/HARD/GOOD/EASY`.
  - Note: Browser screenshot capture was not executed from this environment; evidence is CLI/API/SSM based and documented in `docs/manual-e2e/phase2-staging-evidence.md`.

**TASK-2-VALIDATE-002: RDS Backup Test**
- Status: [X] DONE
- Depends: TASK-2-VALIDATE-001
- Owner: DevOps
- Est: 1h
- Description:
  - AWS console: RDS → create manual snapshot
  - Verify snapshot created
  - (Don't restore, just verify capability)
  - Evidence (2026-04-11): manual snapshot `khaleoapp-staging-db-manual-20260411144139` created and reached `available` at `2026-04-11T07:41:53.088000+00:00`.

**TASK-2-VALIDATE-003: CloudWatch Logs Review**
- Status: [X] DONE
- Depends: TASK-2-VALIDATE-001
- Owner: DevOps
- Est: 0.5h
- Description:
  - CloudWatch: check EC2 logs, RDS logs
  - No errors, warnings acceptable
  - Evidence (2026-04-11): reviewed EC2 runtime logs (SSM output), RDS error logs, and CloudWatch metrics (EC2/RDS CPU + RDS connections).
  - Finding: non-fatal backend errors exist for async DynamoDB activity-log writes (`dynamodb:PutItem` denied on `StudyActivityLog`), while core auth/deck/card/study flows succeed.

---

## Phase 3: Production Deployment (Week 3)

### Layer 3.1: Production AWS (copy of staging, prod domains)

**TASK-3-INFRA-001: Prod VPC + Network**
- Status: DONE
- Depends: TASK-2-VALIDATE-003
- Owner: DevOps
- Est: 1h
- Description:
  - Copy staging config, change CIDR to 10.1.0.0/16
  - Change tags, names to prod

**TASK-3-INFRA-002: Prod RDS MySQL**
- Status: DONE
- Depends: TASK-3-INFRA-001
- Owner: DevOps
- Est: 0.5h
- Description:
  - db.t3.micro, same as staging
  - Backup: 7 days
  - Multi-AZ: consider for HA (or leave false for cost)
  - Encryption: true

**TASK-3-INFRA-003: Prod EC2**
- Status: DONE
- Depends: TASK-3-INFRA-001
- Owner: DevOps
- Est: 0.5h
- Description:
  - t3.micro, same as staging

**TASK-3-INFRA-004: Prod S3 + CloudFront**
- Status: DONE
- Depends: TASK-3-INFRA-001
- Owner: DevOps
- Est: 1h
- Description:
  - S3: khaleo-prod-frontend
  - CloudFront: khaleoshop.click
  - ACM: prod certificate

**TASK-3-INFRA-005: Prod Route 53**
- Status: DONE
- Depends: TASK-3-INFRA-004
- Owner: DevOps
- Est: 0.5h
- Description:
  - khaleoshop.click → CloudFront
  - api.khaleoshop.click → EC2 EIP

**TASK-3-INFRA-006: Terraform Apply (Prod)**
- Status: DONE
- Depends: TASK-3-INFRA-005
- Owner: DevOps
- Est: 1h
- Description:
  - terraform apply -var-file=env/prod.tfvars
  - Verify all prod resources

---

### Layer 3.2: SSL/DNS & Nginx

**TASK-3-SSL-001: ACM Certificates**
- Status: DONE
- Depends: TASK-3-INFRA-006
- Owner: DevOps
- Est: 0.5h
- Description:
  - AWS ACM: request certificates for khaleoshop.click, *.khaleoshop.click
  - Verify via DNS (Route 53 auto)
  - Note certificate ARN

**TASK-3-SSL-002: Nginx SSL + Certbot (Prod EC2)**
- Status: DONE
- Depends: TASK-3-SSL-001
- Owner: DevOps
- Est: 1h
- Description:
  - SSH EC2, install Certbot
  - Get Let's Encrypt cert for api.khaleoshop.click
  - Setup auto-renewal
  - Nginx config: listen 443 ssl, proxy_pass localhost:8080
  - Redirect 80 → 443

---

### Layer 3.3: Secrets & Launch

**TASK-3-SECRETS-001: GitHub Secrets (Prod)**
- Status: DONE
- Depends: TASK-3-SSL-002
- Owner: DevOps
- Est: 0.5h
- Description:
  - Add to GitHub Secrets: EC2_HOST_PROD, CF_DIST_ID_PROD, S3_BUCKET_PROD, RDS_HOST_PROD, JWT_SECRET_PROD, etc.
  - Total 13 secrets

**TASK-3-DEPLOY-001: Merge to main + Deploy**
- Status: DONE
- Depends: TASK-3-SECRETS-001
- Owner: Backend + Frontend
- Est: 1h
- Description:
  - Code review dev → main
  - Merge PR to main
  - GitHub Actions auto-deploy: FE to prod S3, BE docker run on prod EC2
  - Monitor Actions logs

**TASK-3-VALIDATE-001: Production Smoke Test**
- Status: DONE
- Depends: TASK-3-DEPLOY-001
- Owner: QA
- Est: 2h
- Description:
  - Open https://khaleoshop.click
  - Login, create deck, add card, study, rate
  - Logout
  - Check all HTTPS, SSL valid
  - CloudWatch monitoring: no errors

**TASK-3-MONITOR-001: Setup CloudWatch Alarms**
- Status: DONE
- Depends: TASK-3-VALIDATE-001
- Owner: DevOps
- Est: 1h
- Description:
  - Alarms: EC2 CPU > 80%, RDS connections > 10, disk space < 1GB
  - Email notifications

---

## Phase 4: Polish & Documentation (Week 4)

### Layer 4.1: Frontend Polish

**TASK-4-FE-001: Mobile Responsive**
- Status: TODO
- Depends: TASK-3-VALIDATE-001
- Owner: Frontend Dev
- Est: 2h
- Description:
  - Test on mobile browser: iOS Safari, Android Chrome
  - Fix breakpoints: tablets, phones
  - Touch-friendly buttons
  - Hide sidebar on mobile

**TASK-4-FE-002: Accessibility (WCAG AA)**
- Status: TODO
- Depends: TASK-4-FE-001
- Owner: Frontend Dev
- Est: 2h
- Description:
  - Semantic HTML
  - ARIA labels
  - Keyboard navigation (Tab, Enter)
  - Color contrast
  - Test with axe DevTools

**TASK-4-FE-003: Performance Optimization**
- Status: TODO
- Depends: TASK-4-FE-002
- Owner: Frontend Dev
- Est: 2h
- Description:
  - Code splitting: lazy load tabs
  - Images: optimize, lazy load
  - Bundle size: analyze, minify
  - Lighthouse: aim for 90+

---

### Layer 4.2: Backend Polish

**TASK-4-BE-001: Add Caching**
- Status: TODO
- Depends: TASK-3-VALIDATE-001
- Owner: Backend Dev
- Est: 2h
- Description:
  - DeckStats: cache 5 min (using @Cacheable)
  - User: cache 10 min
  - Invalidate on update

**TASK-4-BE-002: Rate Limiting**
- Status: TODO
- Depends: TASK-4-BE-001
- Owner: Backend Dev
- Est: 1h
- Description:
  - Add Bucket4j or similar
  - Rate limit: 100 req/min per user
  - 401 response when exceeded

**TASK-4-BE-003: Input Validation & XSS Prevention**
- Status: TODO
- Depends: TASK-4-BE-002
- Owner: Backend Dev
- Est: 1h
- Description:
  - Add @Validated, @NotBlank, @Size annotations
  - Sanitize input (escape HTML)
  - Use parameterized queries (JPA handles this)

---

### Layer 4.3: Documentation

**TASK-4-DOCS-001: User Guide**
- Status: TODO
- Depends: TASK-3-VALIDATE-001
- Owner: DevOps + QA
- Est: 2h
- Description:
  - Markdown: docs/USER_GUIDE.md
  - How to login, create deck, add card, study
  - Screenshots
  - FAQ

**TASK-4-DOCS-002: API Documentation (Swagger)**
- Status: TODO
- Depends: TASK-4-BE-003
- Owner: Backend Dev
- Est: 1h
- Description:
  - Add springdoc-openapi, @OpenAPI annotations
  - Swagger UI at /swagger-ui.html
  - Export as OpenAPI spec

**TASK-4-DOCS-003: Deployment Guide**
- Status: TODO
- Depends: TASK-4-DOCS-002
- Owner: DevOps
- Est: 1.5h
- Description:
  - Markdown: docs/DEPLOYMENT.md
  - Terraform setup, GitHub Actions, prod launch checklist

---

## Task Summary

**Total Tasks:** 93
**Phase 1 (Dev):** 50 tasks, ~30-35 days effort
**Phase 2 (Staging):** 20 tasks, ~15-20 days effort
**Phase 3 (Prod):** 12 tasks, ~10-12 days effort
**Phase 4 (Polish):** 11 tasks, ~15-20 days effort

**Estimated Timeline:** 13 working days (~2.5 weeks)

