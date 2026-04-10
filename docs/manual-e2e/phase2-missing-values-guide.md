# Phase 2 Missing Values Guide (What I Cannot Auto-Fetch)

This guide lists values that cannot be discovered from local code alone and how to get them in AWS/GitHub.

## Required Values

- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `S3_BUCKET_STAGING`
- `CF_DIST_ID_STAGING`
- `EC2_HOST_STAGING`
- `EC2_SSH_USER`
- `EC2_SSH_PRIVATE_KEY` (PEM file content)
- `RDS_HOST_STAGING`
- `RDS_DB_NAME` (default: `khaleoapp`)
- `RDS_DB_USER` (default: `app_user`)
- `RDS_DB_PASSWORD`
- `JWT_SECRET_STAGING`
- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`
- `tf_state_bucket_name` (for Terraform backend)

## Step-by-step: Get each value

### 1) AWS credentials (for GitHub Actions)
1. AWS Console -> IAM -> Users -> your CI user.
2. Security credentials -> Create access key.
3. Copy:
   - Access key id -> `AWS_ACCESS_KEY_ID`
   - Secret access key -> `AWS_SECRET_ACCESS_KEY`

### 2) Terraform state bucket name
1. Choose a globally unique bucket name, e.g. `khaleoapp-tf-state-<random>`.
2. Use that value for `tf_state_bucket_name`.

### 3) Staging infra values (after `terraform apply`)
Run:

```powershell
terraform -chdir="infra/terraform/app" output
```

Collect:
- EC2 public host/ip -> `EC2_HOST_STAGING` (from `api_public_ip`)
- RDS endpoint -> `RDS_HOST_STAGING` (from `rds_endpoint`, strip port if present)
- Frontend bucket -> `S3_BUCKET_STAGING` (from `frontend_bucket_name`)
- CloudFront distribution id -> `CF_DIST_ID_STAGING` (from `cloudfront_distribution_id`)

### 4) EC2 SSH
- `EC2_SSH_USER`: usually `ec2-user` for Amazon Linux.
- Keep your `.pem` file locally.
- The wizard stores `EC2_SSH_PRIVATE_KEY_PATH` and the secrets script uploads it as `EC2_SSH_PRIVATE_KEY` automatically.

### 5) JWT secret
Generate a random secret (at least 32 chars). Example PowerShell:

```powershell
[Convert]::ToBase64String((1..48 | ForEach-Object { Get-Random -Maximum 256 }))
```

Use as `JWT_SECRET_STAGING`.

### 6) Docker Hub
- `DOCKERHUB_USERNAME`: your Docker Hub username.
- `DOCKERHUB_TOKEN`: Docker Hub personal access token.

## One-command flow after you collect values

1. Run config wizard:

```powershell
PowerShell -ExecutionPolicy Bypass -File "scripts/setup-staging-config.ps1"
```

2. Push secrets to GitHub environment `staging`:

```powershell
PowerShell -ExecutionPolicy Bypass -File "scripts/set-github-staging-secrets.ps1"
```

3. Trigger staging deploy (develop branch push).

## Faster path (less manual)

After Terraform apply, run:

```powershell
PowerShell -ExecutionPolicy Bypass -File "scripts/build-staging-secrets-from-terraform.ps1"
```

This auto-fills from Terraform outputs:
- `S3_BUCKET_STAGING`
- `CF_DIST_ID_STAGING`
- `EC2_HOST_STAGING`
- `RDS_HOST_STAGING`

Then run:

```powershell
PowerShell -ExecutionPolicy Bypass -File "scripts/set-github-staging-secrets.ps1"
```
