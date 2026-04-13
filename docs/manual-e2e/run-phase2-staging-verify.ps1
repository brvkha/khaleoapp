param(
    [string]$RepoRoot = "C:\Workspace\FPT\github\khaleoapp"
)

$ErrorActionPreference = "Stop"

Write-Host "[1/5] Terraform fmt + validate (bootstrap)"
terraform -chdir="$RepoRoot/infra/terraform/bootstrap" fmt -check -recursive
terraform -chdir="$RepoRoot/infra/terraform/bootstrap" validate

Write-Host "[2/5] Terraform fmt + validate (app, backend disabled)"
terraform -chdir="$RepoRoot/infra/terraform/app" fmt -check -recursive
terraform -chdir="$RepoRoot/infra/terraform/app" init -backend=false
terraform -chdir="$RepoRoot/infra/terraform/app" validate

Write-Host "[3/5] Parse workflow YAML files"
if (Get-Command ConvertFrom-Yaml -ErrorAction SilentlyContinue) {
    Get-Content "$RepoRoot/.github/workflows/deploy-frontend.yml" -Raw | ConvertFrom-Yaml | Out-Null
    Get-Content "$RepoRoot/.github/workflows/deploy-backend.yml" -Raw | ConvertFrom-Yaml | Out-Null
} else {
    Write-Host "ConvertFrom-Yaml not available in this PowerShell. Skip YAML parse check."
}

Write-Host "[4/5] Frontend build gate"
Push-Location "$RepoRoot/frontend"
npm ci
npm run build
Pop-Location

Write-Host "[5/5] Backend package gate"
Push-Location "$RepoRoot/backend"
mvn -B -ntp clean package -DskipTests
Pop-Location

Write-Host "Done. For live staging deploy and AWS checks, continue with docs/manual-e2e/phase2-staging-runbook.md"


