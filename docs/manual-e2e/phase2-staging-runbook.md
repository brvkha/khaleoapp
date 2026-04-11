# Phase 2 Staging Runbook (Operational)

Date: 2026-04-11

## Runtime context
This runbook keeps manual validation steps, but operational actions can be executed from this workspace using AWS CLI + SSM when credentials are available.

## 0) Fill missing values once (guided)

Use the guide:
- `docs/manual-e2e/phase2-missing-values-guide.md`

Then run wizard to generate all local config files:

```powershell
PowerShell -ExecutionPolicy Bypass -File "scripts/setup-staging-config.ps1"
```

This writes:
- `infra/terraform/bootstrap/terraform.tfvars`
- `infra/terraform/app/backend-staging.hcl`
- `infra/terraform/app/env/staging.tfvars`
- `frontend/.env.staging`
- `docs/manual-e2e/staging-secrets.local.env` (local only)

## 1) Terraform apply/destroy/recreate via GitHub Actions (recommended)

Use workflow `.github/workflows/terraform-staging.yml`:

```powershell
Set-Location "C:\Workspace\FPT\github\khaleoapp"
gh workflow run terraform-staging.yml --ref develop -f action=apply -f auto_approve=true
gh workflow run terraform-staging.yml --ref develop -f action=destroy -f auto_approve=true
gh workflow run terraform-staging.yml --ref develop -f action=recreate -f auto_approve=true
```

Notes:
- Workflow always reapplies `bootstrap/` before app action to keep backend state path reproducible.
- `recreate` runs `destroy` then `apply` with the same CI tfvars generation path.
- On successful `apply`/`recreate` (`auto_approve=true`), workflow calls both `deploy-frontend.yml` and `deploy-backend.yml` via `workflow_call` in the same pipeline.

## 1.1) Manual Terraform apply fallback (local credentials)

```powershell
Set-Location "C:\Workspace\FPT\github\khaleoapp"
terraform -chdir="infra/terraform/bootstrap" init
terraform -chdir="infra/terraform/bootstrap" apply -var-file="terraform.tfvars"
terraform -chdir="infra/terraform/app" init -reconfigure -backend-config="backend-staging.hcl"
terraform -chdir="infra/terraform/app" plan -var-file="env/staging.tfvars"
terraform -chdir="infra/terraform/app" apply -var-file="env/staging.tfvars"
terraform -chdir="infra/terraform/app" output
```

## 2) Push GitHub staging secrets automatically

Option A (recommended): auto-detect infra values from Terraform output, then build secrets file:

```powershell
PowerShell -ExecutionPolicy Bypass -File "scripts/build-staging-secrets-from-terraform.ps1"
```

Option B: use values already entered by wizard.

Then push secrets:

```powershell
PowerShell -ExecutionPolicy Bypass -File "scripts/set-github-staging-secrets.ps1"
```

## 3) Nginx + TLS bootstrap is now automated (no manual TASK-2-CONFIG-003 step)

```powershell
Set-Location "C:\Workspace\FPT\github\khaleoapp"
# Ensure Terraform app module is applied first (creates SSM document `${project}-${environment}-nginx-tls-bootstrap`)
terraform -chdir="infra/terraform/app" output nginx_tls_ssm_document_name

# Trigger backend deploy workflow (this now auto-runs nginx/tls SSM bootstrap before docker deploy)
gh workflow run deploy-backend.yml --ref develop

# Optional verification from CLI
aws ssm list-command-invocations --details --max-results 10
curl.exe -I https://api-staging.khaleoshop.click/
```

Automation notes:
- The deploy workflow calls Terraform-managed SSM document `${project}-${environment}-nginx-tls-bootstrap`.
- The document is idempotent (`--keep-until-expiring`, safe to re-run every deploy).
- If `TLS_EMAIL_STAGING` is absent, certbot uses non-email registration mode.

## 4) Trigger GitHub Actions deploy (frontend)

Normally no manual trigger is required after Terraform apply/recreate because the infra workflow now triggers both app deploy workflows automatically.

Frontend build now uses `VITE_API_BASE_URL_STAGING` from GitHub environment secrets (fallback: `https://api-staging.khaleoshop.click`) and fails if built assets still contain `localhost:8080`.

Manual fallback (if needed):

```powershell
git checkout develop
git commit --allow-empty -m "chore: trigger staging deploy"
git push origin develop
```

Then check:
- Frontend workflow: S3 sync + CloudFront invalidation successful.
- Backend workflow: Docker push + SSM deploy successful, including nginx/tls bootstrap step.

## 5) Execute staging validation checklist
Use:
- `docs/manual-e2e/phase2-staging-checklist.md`
- `docs/manual-e2e/phase2-staging-evidence.md`

Status update (2026-04-11):
- `TASK-2-CONFIG-003` completed via SSM (`4bcd66ad-4cab-4192-99c3-22d1e80304e7`)
- `TASK-2-VALIDATE-001` validated from this environment using API + runtime log evidence
- `TASK-2-VALIDATE-002` completed with manual snapshot `khaleoapp-staging-db-manual-20260411144139`
- `TASK-2-VALIDATE-003` completed as log/metric review with one follow-up finding: async activity logging to DynamoDB is access-denied and emits `activity_log_dead_letter`

## 6) Rollback references
- Frontend rollback: redeploy previous commit on `develop`.
- Backend rollback: rerun `deploy-backend.yml` with previous commit SHA; workflow re-applies nginx/tls idempotently and restarts container on selected tag.
