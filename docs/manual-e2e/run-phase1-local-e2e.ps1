param(
    [string]$RepoRoot = "C:\Workspace\FPT\github\khaleoapp"
)

$ErrorActionPreference = "Stop"

Write-Host "[1/5] Start local MySQL via docker compose"
Set-Location $RepoRoot
docker compose up -d

Write-Host "[2/5] Run backend test gate (unit + swagger integration)"
Set-Location "$RepoRoot\backend"
./mvnw -q "-Dtest=PrivateDeckCrudServiceTest,RelationalPersistenceCardServiceTest,StudyRatingServiceTest,SwaggerDocsIntegrationIT" test

Write-Host "[3/5] Start backend in dedicated terminal/session"
Write-Host "Command: Set-Location '$RepoRoot\backend'; ./mvnw spring-boot:run"

Write-Host "[4/5] Start frontend in dedicated terminal/session"
Write-Host "Command: Set-Location '$RepoRoot\frontend'; npm install; npm run dev"

Write-Host "[5/5] Manual browser checklist"
Write-Host "Use docs/manual-e2e/phase1-local-checklist.md and collect screenshots/proofs."

