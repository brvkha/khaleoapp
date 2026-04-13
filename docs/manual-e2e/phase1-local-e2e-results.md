# Phase 1 Local E2E Results / Evidence

Date: 2026-04-10
Scope: TASK-1-TEST-005

## Automated Gate (executed in this implementation)

Command:
```powershell
Set-Location "C:\Workspace\FPT\github\khaleoapp\backend"
mvn "-Dtest=PrivateDeckCrudServiceTest,RelationalPersistenceCardServiceTest,StudyRatingServiceTest,SwaggerDocsIntegrationIT" test
```

Result (actual run on 2026-04-10):
- Maven goal: `test`
- Target classes: `PrivateDeckCrudServiceTest, RelationalPersistenceCardServiceTest, StudyRatingServiceTest, SwaggerDocsIntegrationIT`
- Total tests: `12`
- Failures: `0`
- Errors: `0`
- Skipped: `0`
- Build status: `BUILD SUCCESS`
- Duration: `43.052 s`

Important runtime notes from test log:
- Testcontainers detected Docker Desktop and started MySQL `8.0.39` container successfully.
- `SwaggerDocsIntegrationIT` verified `/swagger-ui/index.html` and `/v3/api-docs`.

Raw details are stored in:
- `backend/target/surefire-reports/`

## Manual Browser Run (to be filled when executed locally)

### Checklist outcome
- [ ] Flow 1 completed
- [ ] Flow 2 completed
- [ ] Flow 3 completed

### Command/session evidence
- Backend run command:
```powershell
Set-Location "C:\Workspace\FPT\github\khaleoapp\backend"
mvn spring-boot:run
```
- Frontend run command:
```powershell
Set-Location "C:\Workspace\FPT\github\khaleoapp\frontend"
npm install
npm run dev
```

### Screenshots / artifacts
- [ ] `docs/manual-e2e/artifacts/login-success.png`
- [ ] `docs/manual-e2e/artifacts/deck-card-created.png`
- [ ] `docs/manual-e2e/artifacts/study-rate.png`
- [ ] `docs/manual-e2e/artifacts/logout-redirect.png`
- [ ] `docs/manual-e2e/artifacts/refresh-network.png`

### Notes
- This file is designed to store reproducible command evidence and manual QA artifacts for Phase 1 local validation.

