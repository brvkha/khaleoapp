# Phase 2 Staging Evidence Log

Date: 2026-04-11
Scope: TASK-2-CONFIG-003, TASK-2-VALIDATE-001, TASK-2-VALIDATE-002, TASK-2-VALIDATE-003

## Local preflight checks executed in this implementation
- Terraform format/validate checks: see command section below.
- Workflow YAML syntax check: parsed by PowerShell YAML parser.
- Frontend build and backend package: executed locally.

## Commands executed

```powershell
terraform -chdir="infra/terraform/bootstrap" validate
terraform -chdir="infra/terraform/app" validate
terraform -chdir="infra/terraform/app" init -reconfigure -backend-config="backend-staging.hcl" -input=false
terraform -chdir="infra/terraform/app" plan -input=false -var-file="env/staging.tfvars" -out="staging.tfplan"
terraform -chdir="infra/terraform/app" apply -input=false -auto-approve "staging.tfplan"
aws route53 list-resource-record-sets --hosted-zone-id Z09479913APS5DRNMZYZ3
gh run view 24232134510 --repo brvkha/khaleoapp
gh run view 24235846860 --repo brvkha/khaleoapp
```

## Staging deploy evidence

### Terraform outputs
- frontend_bucket_name: `khaleoapp-staging-frontend-817888697629`
- cloudfront_distribution_id: `E2HVOUMS7OZFLJ`
- api_public_ip: `52.74.64.39`
- backend_instance_id: `i-08d959e33c27ef9de`
- rds_endpoint: `khaleoapp-staging-db.cl8m6uq24vd0.ap-southeast-1.rds.amazonaws.com:3306`

### Frontend deploy workflow
- Run URL: `https://github.com/brvkha/khaleoapp/actions/runs/24232134510`
- Commit SHA: `855640a0c1a8e2fb6c2635d39b61c114688c5db0`
- Result: `success`

### Backend deploy workflow
- Run URL: `https://github.com/brvkha/khaleoapp/actions/runs/24235846860`
- Commit SHA: `62b55d79b34a14e5b29d9508984f27deceaa3690`
- Result: `success`
- Notes: migrated deploy to SSM; fixed EC2 root volume to 20GB to avoid Docker pull `no space left on device`.

### DNS records (Route53)
- `staging.khaleoshop.click` -> Alias A to CloudFront `d3nc3pdqb3qq2e.cloudfront.net`
- `api-staging.khaleoshop.click` -> A `52.74.64.39`
- `stage.khaleoshop.click` -> Alias A via the same CloudFront alias set
- `_e8365...staging.khaleoshop.click` -> CNAME ACM validation
- `khaleoshop.click` currently has only `NS`/`SOA` in this phase (production mapping is Phase 3)

### TASK-2-CONFIG-003 (Nginx + SSL on EC2)
- Execution path: Terraform-managed SSM document (`aws_ssm_document.nginx_tls_bootstrap`) executed from backend deploy workflow (no SSH key).
- Instance: `i-08d959e33c27ef9de` (Amazon Linux 2023, SSM Online).
- Example successful command id from earlier manual migration baseline: `4bcd66ad-4cab-4192-99c3-22d1e80304e7` (`Status: Success`, `ResponseCode: 0`).
- Certbot result: certificate issued for `api-staging.khaleoshop.click`, expiry `2026-07-10`, files at `/etc/letsencrypt/live/api-staging.khaleoshop.click/`.
- Nginx config reconciled at `/etc/nginx/conf.d/khaleo-api.conf` with `80 -> 443` redirect and reverse proxy to `http://127.0.0.1:8080`.
- External verification from this workspace: `curl -I https://api-staging.khaleoshop.click/` -> `HTTP/1.1 404`, `Server: nginx/1.28.2` (expected because backend root path is not mapped).
- Idempotency note: workflow re-runs TLS/bootstrap step each deploy; cert issuance uses `--keep-until-expiring` so destroy/recreate and steady-state redeploy share the same command path.

### Already satisfied in repo/config
- `stage.khaleoshop.click` is wired through `infra/terraform/app/env/staging.tfvars` as an additional frontend domain name.
- The Terraform app stack already provisions the matching Route 53 alias records for every frontend alias.
- `infra/nginx/khaleo-staging.conf.example` exists as the EC2 reverse-proxy source config for `TASK-2-CONFIG-003`.

### Manual integration validation
- Frontend domain checks (2026-04-11):
  - `https://staging.khaleoshop.click` -> `200 OK`
  - `https://stage.khaleoshop.click` -> `200 OK`
- Auth API checks:
  - `POST /api/v1/auth/login` success (`expiresIn=900`)
  - `POST /api/v1/auth/refresh` success and returns new `accessToken` (`refreshToken` currently `null` in response payload)
  - `POST /api/v1/auth/logout` succeeds when using the login refresh token
- Runtime API-flow evidence from EC2 logs (SSM command `7f573019-b999-42c5-9214-81ae963634c5`):
  - `auth_login_success`: observed
  - `relational_deck_create_success`: observed
  - `relational_card_create_success`: observed
  - Study ratings observed for all values: `AGAIN`, `HARD`, `GOOD`, `EASY`
  - `auth_refresh_success`: observed
- Structured extraction from the same SSM output:
  - `authLoginSuccessCount=6`
  - `deckCreateSuccessCount=2`
  - `cardCreateSuccessCount=2`
  - `ratingAgainCount=1`, `ratingHardCount=1`, `ratingGoodCount=1`, `ratingEasyCount=1`
  - `authRefreshSuccessCount=2`

### RDS backup validation
- Create snapshot command executed:
  - `aws rds create-db-snapshot --db-instance-identifier khaleoapp-staging-db --db-snapshot-identifier khaleoapp-staging-db-manual-20260411144139`
- Final status (after waiter):
  - Snapshot id: `khaleoapp-staging-db-manual-20260411144139`
  - Snapshot status: `available`
  - Create time: `2026-04-11T07:41:53.088000+00:00`
  - Engine: `mysql` (`AllocatedStorage=20`)

### CloudWatch review
- Log sources reviewed:
  - EC2 backend runtime logs via SSM command output (`CommandId: 7f573019-b999-42c5-9214-81ae963634c5`)
  - RDS error logs via `describe-db-log-files` + `download-db-log-file-portion`
- Metric summary (last 6 hours, `ap-southeast-1`):
  - EC2 `CPUUtilization`: `max=7.299878335361078`, `avg=0.5203730098467879` (`72` points)
  - RDS `CPUUtilization`: `max=6.483333333333334`, `avg=3.238100704048138` (`72` points)
  - RDS `DatabaseConnections`: `max=11`, `avg=10.008333333333333` (`72` points)
- RDS log findings:
  - `error/mysql-error.log`: empty
  - `error/mysql-error-running.log.2026-04-10.10`: one warning (`IP address '10.0.1.35' could not be resolved`)
- EC2 backend log findings:
  - User-flow events for auth/deck/card/study are present and successful.
  - Non-fatal errors exist for async activity logging (`dynamodb:PutItem` access denied on `StudyActivityLog` in `us-east-1`, with `activity_log_dead_letter` entries).
  - These errors did not block login/deck/card/study API path but should be tracked as operational debt.

