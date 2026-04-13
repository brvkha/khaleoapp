# PLAN - Implementation Roadmap

## Overview

Dự án KhaLeo flashcard app. Roadmap chia thành 3 phase (Local Dev → Staging → Production).

---

## Phase 1: Local Development Setup (Week 1)

### 1.1 Backend Foundation
- [ ] **Initialize Spring Boot project**
  - JDK 17, Maven, Spring Web, Spring Data JPA, Spring Security, MySQL Driver
  - Basic project structure: controller/, service/, entity/, repository/, config/
  
- [ ] **Database Setup (Local)**
  - MySQL Docker container (docker-compose.yml)
  - Flyway migration V1 (users, decks, cards, card_learning_states, refresh_tokens, login_attempts)
  - Seed data: test user (user@test.com / password123)

- [ ] **Auth Service (Spring Security + JWT)**
  - JwtTokenService: generate access/refresh tokens
  - AuthenticationService: login/logout logic
  - TokenRefreshService: rotate refresh token
  - LoginLockoutService: 5 failed attempts → lock 15 min
  - Database: refresh_tokens table, login_attempts table
  - API: POST /auth/login, POST /auth/refresh, POST /auth/logout

- [ ] **Basic CRUD: Deck**
  - Entity: Deck (id, user_id, name, description, is_public, created_at, updated_at)
  - Repository: DeckRepository (findByUserId, search by name)
  - Service: PrivateDeckCrudService (create, read, update, delete)
  - Controller: PrivateWorkspaceController (GET/POST/PUT/DELETE /api/v1/private/decks)
  - API: paging, search, authorization check

- [ ] **Basic CRUD: Card**
  - Entity: Card (id, deck_id, term, answer, version, created_at, updated_at)
  - Repository: CardRepository (findByDeckId, search)
  - Service: CardService (CRUD)
  - Controller: CardController (GET/POST/PUT/DELETE /api/v1/decks/{id}/cards)
  - API: search + pagination

- [ ] **CORS Configuration**
  - Allow localhost:5173 (React dev)
  - Credentials: true (for httpOnly cookies)

### 1.2 Frontend Foundation
- [ ] **Initialize React + Vite project**
  - Node 18, React 18, TypeScript, Tailwind, React Router, Zustand/Redux
  - Project structure: features/, services/, components/, store/
  - Vite config: proxy /api to localhost:8080

- [ ] **Auth Flow**
  - Login page: email + password form
  - API client: privateWorkspaceApi, authApi, studySessionApi
  - Auth store: login(), logout(), setCurrentUser(), refreshToken()
  - Token management: access token in memory, refresh token auto via cookie
  - Auth guard: RequireAuth (redirect /login if not authenticated)
  - Logout: clear token + navigate /login

- [ ] **Layout & Navigation**
  - Header: KhaLeo logo, user email, logout button
  - Sidebar: Decks | Cards | Study | (Admin nếu role=ADMIN)
  - Main content area + notification toast center

- [ ] **Decks Tab (MVP)**
  - List decks (GET /api/v1/private/decks?q=&page=0&size=50)
  - Create deck modal (POST)
  - Edit deck inline (PUT)
  - Delete deck confirm (DELETE)
  - View mode: Card | Table
  - Empty state: "No decks yet..."

- [ ] **Cards Tab (MVP)**
  - Left panel: deck list (select deck)
  - Right panel: card table (search + pagination)
  - Create card modal (POST)
  - Edit card inline (PUT)
  - Delete card confirm (DELETE)
  - Empty state: "Select a deck to view cards"

- [ ] **Study Tab - Workspace (MVP)**
  - List decks + stats (learning, review, new_cards)
  - "Start session" button → navigate to session page
  - View mode: Card | Table

- [ ] **Study Tab - Session (MVP)**
  - Load cards from API (GET /api/v1/study-session/decks/{deckId}/next-cards)
  - Display current card (front side)
  - Click to reveal back
  - 4 rating buttons: Again | Hard | Good | Easy
  - Submit rating (POST /api/v1/study-session/cards/{cardId}/rate)
  - Load next card
  - "Session complete" when no more cards

### 1.3 FSRS v6 Engine
- [ ] **SpacedRepetitionService (Core Logic)**
  - Implement all formulas: init stability, init difficulty, next difficulty, recall stability, forget stability, etc.
  - Apply FSRS v6 logic per rating (Again/Hard/Good/Easy)
  - Calculate next review date + interval
  - Handle NEW, LEARNING, REVIEW, RELEARNING states
  - Same-day handling (learning phase)

- [ ] **CardLearningState Entity**
  - Entity class: state, fsrs_stability, fsrs_difficulty, fsrs_reps, fsrs_lapses, learning_step_good_count, last_reviewed_at, next_review_at, version
  - Repository: findByUserIdAndCardId, queries for due cards

- [ ] **Study Session APIs**
  - GET /api/v1/study-session/decks/{deckId}/next-cards → NextCardsService
  - GET /api/v1/study-session/cards/{cardId}/preview-ratings → show next review for each rating
  - POST /api/v1/study-session/cards/{cardId}/rate → StudyRatingService (apply FSRS, update state, persist)

- [ ] **DeckStatsService**
  - Count learning, review, new cards per deck per user
  - API: GET /api/v1/private/decks/{id}/stats

### 1.4 Testing & Local Validation
- [ ] **Backend unit tests**
  - SpacedRepetitionService: test FSRS logic (new card, learning, review, relearning, again, hard, good, easy)
  - Service layer tests (DeckService, CardService)
  
- [ ] **Frontend component tests**
  - Auth flow, tab navigation, card CRUD, study session

- [ ] **Integration tests (Postman/REST API)**
  - Auth: login → get token, refresh token, logout
  - Deck CRUD
  - Card CRUD
  - Study session: next cards, rate card

- [ ] **Manual testing**
  - Dev environment (localhost:5173 + localhost:8080)
  - Check CORS, token management, error handling

---

## Phase 2: Staging Deployment (Week 2)

### 2.1 AWS Infrastructure (Terraform)
- [ ] **Bootstrap (S3 + DynamoDB lock)**
  - Create S3 bucket for Terraform state (global unique name)
  - Create DynamoDB table for lock
  - Setup local test

- [ ] **Staging VPC + Network**
  - VPC, subnets (public + private), IGW, route table
  - Security groups (EC2: 80/443, RDS: 3306)

- [ ] **Staging RDS MySQL**
  - db.t3.micro, 20GB storage
  - Multi-AZ: No (cost saving)
  - Backup: 7 days
  - Subnet group: private subnets

- [ ] **Staging EC2**
  - t3.micro, Amazon Linux 2023
  - Security group, Elastic IP
  - IAM role for SSM access
  - User data: install Docker, Nginx

- [ ] **Staging S3 + CloudFront**
  - S3 bucket: staging frontend build
  - CloudFront: OAC for S3, HTTPS via ACM
  - Route 53: staging.khaleoshop.click, stage.khaleoshop.click → CloudFront
  - api-staging.khaleoshop.click → EC2 Elastic IP

### 2.2 GitHub Actions Workflows
- [ ] **Deploy Frontend Workflow**
  - Trigger: push to `develop` branch + frontend/** path
  - Build React (npm run build)
  - Sync dist/ to S3_BUCKET_STAGING
  - CloudFront invalidation
  - Test: check S3 bucket has new files

- [ ] **Deploy Backend Workflow**
  - Trigger: push to `develop` branch + backend/** path
  - Build JAR (mvnw clean package)
  - Build Docker image (khaleo/backend:latest, khaleo/backend:{sha})
  - Push to Docker Hub
  - SSH to EC2 staging: docker pull → stop old → run new
  - Test: check container running on 8080

### 2.3 Environment-Specific Configuration
- [ ] **Backend application-staging.yml**
  - DB_HOST, DB_NAME, DB_USER, DB_PASSWORD (from RDS)
  - Spring profile: staging
  - JWT_SECRET (staging-specific)
  - Logging level

- [ ] **Frontend .env.staging**
  - VITE_API_BASE_URL=https://api-staging.khaleoshop.click

- [ ] **Nginx on EC2**
  - Reverse proxy: api-staging.khaleoshop.click → localhost:8080
  - SSL/TLS: Let's Encrypt + Certbot
  - Automated via Terraform-managed SSM document + backend deploy workflow (no manual SSH/one-off command)

### 2.4 Staging Validation
- [ ] **Full integration test on staging**
  - Login to https://staging.khaleoshop.click
  - Create deck, add card, study session
  - Rate cards, check stats
  - Check token refresh
  - Check error handling (logout on 401, etc.)

- [ ] **Database backup test**
  - Trigger manual backup on RDS
  - Test restore (not on prod, just verify capability)

---

## Phase 3: Production Deployment (Week 3)

### 3.1 Production AWS Infrastructure
- [ ] **Prod VPC + Network** (same as staging, different CIDR)
- [ ] **Prod RDS MySQL**
  - db.t3.micro
  - Multi-AZ: consider for HA
  - Backup: 7 days
  - Encryption: enabled
  
- [ ] **Prod EC2 + EIP**
- [ ] **Prod S3 + CloudFront**
  - khaleoshop.click → CloudFront
  - api.khaleoshop.click → EC2

### 3.2 DNS & SSL
- [ ] **Route 53 records**
  - khaleoshop.click (A) → CloudFront
  - api.khaleoshop.click (A) → EC2 EIP
  - ACM certificates for both domains

- [ ] **Nginx SSL on EC2**
  - Let's Encrypt certificate
  - Auto-renewal via Certbot

### 3.3 Secrets Management
- [ ] **GitHub Secrets setup**
  - AWS credentials (prod)
  - Docker Hub credentials
  - EC2 SSH key
  - RDS credentials
  - JWT_SECRET (prod, different from staging)
  - All S3 bucket names, CloudFront IDs, EC2 hosts

### 3.4 Production Validation
- [ ] **Pre-launch checklist**
  - Database backup working
  - SSL/TLS valid on both domains
  - Auth flow verified
  - CORS configured for prod domains
  - Monitoring setup (CloudWatch alarms)

- [ ] **Launch**
  - Merge to `main` branch
  - GitHub Actions deploys automatically
  - Verify https://khaleoshop.click works
  - Test all features

- [ ] **Post-launch monitoring**
  - Check logs (CloudWatch, EC2 syslog)
  - Monitor RDS metrics (CPU, connections)
  - Monitor EC2 metrics
  - Test logout on 401 (refresh token expiry)

---

## Phase 4: Polish & Monitoring (Week 4)

### 4.1 Frontend Enhancements
- [ ] **Keyboard shortcuts** (study: space to flip card)
- [ ] **Accessibility** (WCAG AA)
- [ ] **Mobile responsive** (test on mobile browser)
- [ ] **Offline support** (service worker cache for offline access to existing data)
- [ ] **Performance** (lazy loading, code splitting)

### 4.2 Backend Enhancements
- [ ] **Caching** (Redis optional, local cache for stats)
- [ ] **Rate limiting** (prevent brute force API calls)
- [ ] **Input validation** (sanitize XSS)
- [ ] **API versioning** (ready for v2 in future)
- [ ] **Logging** (structured logs for debugging)

### 4.3 Monitoring & Alerting
- [ ] **CloudWatch dashboards**
  - EC2 CPU, memory, disk
  - RDS CPU, connections, storage
  - API response times

- [ ] **CloudWatch alarms**
  - High CPU usage
  - DB connection threshold
  - Low disk space

- [ ] **Error tracking** (optional: Sentry integration)

### 4.4 Documentation
- [ ] **User guide** (how to use KhaLeo)
- [ ] **Admin guide** (how to manage users/decks)
- [ ] **API documentation** (auto-generated from Swagger)
- [ ] **Troubleshooting guide**

---

## Task Dependencies

```
Phase 1 (Parallel)
├─ Backend Auth → Deck CRUD → Card CRUD → FSRS Engine → Testing
└─ Frontend Setup → Auth UI → Decks UI → Cards UI → Study UI → Testing

         ↓
         
Phase 2 (Sequential)
├─ AWS Infra (Terraform)
├─ GitHub Actions (Deploy FE/BE)
├─ Env Config
└─ Staging Validation

         ↓
         
Phase 3 (Sequential)
├─ Prod Infrastructure
├─ DNS/SSL
├─ Secrets Setup
└─ Launch + Monitoring

         ↓
         
Phase 4 (Parallel)
├─ Frontend Polish
├─ Backend Polish
├─ Monitoring
└─ Documentation
```

---

## Success Criteria

### Phase 1
- [ ] Backend runs locally, API endpoints work (Postman)
- [ ] Frontend runs locally, all tabs functional
- [ ] FSRS v6 logic passes unit tests
- [ ] Token refresh works correctly
- [ ] 404/500 errors handled gracefully

### Phase 2
- [x] Staging infra + DNS records provisioned (`staging.khaleoshop.click`, `stage.khaleoshop.click`, `api-staging.khaleoshop.click`)
- [x] GitHub Actions FE/BE staging workflows run successfully
- [ ] QA can test on staging.khaleoshop.click (manual flow pending)
- [ ] No console errors in browser (manual verification pending)

Note (2026-04-10): Phase 2 deployment lane is complete; manual QA validation and ops evidence finalization remain open.

### Phase 3
- [ ] Production live at khaleoshop.click
- [ ] All features work on production
- [ ] DNS + SSL certificates valid
- [ ] Backup & restore tested

### Phase 4
- [ ] App performs well under load
- [ ] Monitoring alerts configured
- [ ] Documentation complete
- [ ] Team trained on deployment process

---

## Resource Allocation

| Role | Phase 1 | Phase 2 | Phase 3 | Phase 4 |
|------|---------|---------|---------|---------|
| Backend Dev | 70% | 20% | 20% | 10% |
| Frontend Dev | 70% | 20% | 20% | 20% |
| DevOps/Infra | 10% | 70% | 70% | 20% |
| QA | 10% | 30% | 20% | 30% |

---

## Risk & Mitigation

| Risk | Impact | Mitigation |
|------|--------|-----------|
| Token expiry handling complex | High | Extensive testing of refresh flow |
| FSRS formula implementation bugs | High | Unit tests + comparison with reference |
| AWS cost overrun | Medium | Set up billing alerts, use Free Tier |
| DB migration issues | High | Test Flyway migrations on local/staging first |
| SSL/DNS issues | Medium | Setup 2-3 days before launch |
| Sudden traffic spike | Low | Monitor RDS/EC2, auto-scaling ready for Phase 5 |

---

## Timeline Estimate

- **Phase 1 (Week 1):** 5 days (dev time)
- **Phase 2 (Week 2):** 3 days (infra + testing)
- **Phase 3 (Week 3):** 2 days (deploy + monitoring)
- **Phase 4 (Week 4):** 3 days (polish + docs)

**Total:** ~13 working days (~2.5 weeks in calendar time)

