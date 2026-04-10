# Phase 2 Staging Runbook (Manual)

Date: 2026-04-10

## Why this is manual now
This repository implementation does not include active AWS credentials in the local environment, so real `terraform apply` and live deploy checks cannot be executed from this run.

## 1) Terraform apply on your machine/runner with credentials

```powershell
Set-Location "C:\Workspace\FPT\github\khaleoapp"
Copy-Item "infra\terraform\app\backend-staging.hcl.example" "infra\terraform\app\backend-staging.hcl"
Copy-Item "infra\terraform\app\env\staging.tfvars.example" "infra\terraform\app\env\staging.tfvars"
terraform -chdir="infra/terraform/bootstrap" init
terraform -chdir="infra/terraform/bootstrap" apply -var-file="terraform.tfvars"
terraform -chdir="infra/terraform/app" init -reconfigure -backend-config="backend-staging.hcl"
terraform -chdir="infra/terraform/app" plan -var-file="env/staging.tfvars"
terraform -chdir="infra/terraform/app" apply -var-file="env/staging.tfvars"
terraform -chdir="infra/terraform/app" output
```

## 2) Configure EC2 Nginx and TLS

```bash
sudo cp khaleo-staging.conf.example /etc/nginx/conf.d/khaleo-staging.conf
sudo nginx -t
sudo systemctl restart nginx
sudo certbot --nginx -d api-staging.khaleoshop.click
```

## 3) Trigger GitHub Actions deploy

```powershell
git checkout develop
git commit --allow-empty -m "chore: trigger staging deploy"
git push origin develop
```

Then check:
- Frontend workflow: S3 sync + CloudFront invalidation successful.
- Backend workflow: Docker push + SSH deploy successful.

## 4) Execute staging validation checklist
Use:
- `docs/manual-e2e/phase2-staging-checklist.md`
- `docs/manual-e2e/phase2-staging-evidence.md`

## 5) Rollback references
- Frontend rollback: redeploy previous commit on `develop`.
- Backend rollback: SSH to EC2 and `docker run` previous image tag.

