# Phase 2 Staging Validation Checklist

Date: 2026-04-10
Scope: TASK-2-VALIDATE-001 -> TASK-2-VALIDATE-003

## Preconditions
- [ ] Terraform bootstrap and app stacks have been applied in AWS
- [ ] GitHub Actions secrets for staging are configured
- [ ] `staging.khaleoshop.click` and `api-staging.khaleoshop.click` resolve correctly
- [ ] Backend container is running on EC2 (`docker ps`)

## Flow 1 - End-to-end app validation
- [ ] Open `https://staging.khaleoshop.click`
- [ ] Login using staging test account
- [ ] Create a deck
- [ ] Add at least one card
- [ ] Start study session and submit all 4 ratings
- [ ] Verify deck stats update after rating
- [ ] Logout redirects to login page
- [ ] Wait 14m50s then trigger API call and verify token refresh flow

## Flow 2 - RDS backup test
- [ ] Create manual RDS snapshot for staging DB
- [ ] Snapshot reaches `available` status
- [ ] Record snapshot id and creation time

## Flow 3 - CloudWatch logs review
- [ ] Review EC2 logs around deploy and API requests
- [ ] Review RDS logs for error spikes
- [ ] Confirm no critical errors in test window

## Evidence
- [ ] Screenshot: staging login success
- [ ] Screenshot: deck/card created
- [ ] Screenshot: study rating UI
- [ ] Screenshot: RDS snapshot status
- [ ] Screenshot: CloudWatch log insights query result
- [ ] Attach completed `phase2-staging-evidence.md`

