# Phase 2 Staging Validation Checklist

Date: 2026-04-11
Scope: TASK-2-VALIDATE-001 -> TASK-2-VALIDATE-003

## Preconditions
- [X] Terraform bootstrap and app stacks have been applied in AWS
- [X] GitHub Actions secrets for staging are configured
- [X] `staging.khaleoshop.click` and `api-staging.khaleoshop.click` resolve correctly
- [X] `stage.khaleoshop.click` resolves correctly as the additional frontend alias
- [X] Backend container is running on EC2 (`docker ps` via SSM command `7f573019-b999-42c5-9214-81ae963634c5`)

## Flow 1 - End-to-end app validation (API + runtime evidence)
- [X] Open `https://staging.khaleoshop.click` (HTTP 200)
- [X] Open `https://stage.khaleoshop.click` and confirm it lands on the same frontend build (HTTP 200)
- [X] Login using staging test account (`/api/v1/auth/login` success)
- [X] Create a deck (backend runtime log contains `relational_deck_create_success`)
- [X] Add at least one card (backend runtime log contains `relational_card_create_success`)
- [X] Start study session and submit all 4 ratings (log contains `rating=AGAIN|HARD|GOOD|EASY`)
- [X] Verify deck/study activity emits success events in runtime logs
- [X] Logout API request accepted with login refresh token
- [X] Verify token refresh endpoint returns new access token (`/api/v1/auth/refresh`)

## Flow 2 - RDS backup test
- [X] Create manual RDS snapshot for staging DB
- [X] Snapshot reaches `available` status
- [X] Record snapshot id and creation time

## Flow 3 - CloudWatch/log review
- [X] Review EC2 runtime logs around API requests (via SSM command output)
- [X] Review RDS error logs (`error/mysql-error.log*`)
- [X] Record findings: user flow works, but non-fatal backend errors exist for async DynamoDB activity-log writes (needs follow-up)

## Evidence
- [X] CLI evidence: frontend domain checks (`staging` + `stage` return 200)
- [X] CLI evidence: SSM runtime log sample with auth/deck/card/study flow entries
- [X] CLI evidence: RDS snapshot `khaleoapp-staging-db-manual-20260411144139` available
- [X] CLI evidence: CloudWatch metric summary + RDS log file review
- [X] Attach completed `docs/manual-e2e/phase2-staging-evidence.md`

## Notes
- Browser screenshots were not captured from this environment; validation was completed with AWS CLI + API/SSM evidence to avoid over-claiming UI-level checks.

