# Phase 3 Local Checklist - Listening

Date: 2026-04-21
Feature: `003-listening-dictation`

## Preconditions
- [ ] Backend local MySQL is running and reachable by Spring `test` profile.
- [ ] Frontend dependencies are installed.
- [ ] Seeded learner/admin accounts are available.

## Learner Flow Checks
- [ ] Open `/listening` as learner.
- [ ] Select topic -> exercise -> lesson.
- [ ] Dictation: check correct answer (`Ctrl+Enter`) updates progress.
- [ ] Dictation: skip (`Esc`) updates progress.
- [ ] Transcript: click sentence play button starts playback.
- [ ] Transcript: click word opens dictionary popover.
- [ ] Settings modal changes persist across reload.

## Admin Flow Checks
- [ ] Open `/listening` as admin and confirm CMS surface appears.
- [ ] Confirm sentence reorder reflected in learner order.
- [ ] Confirm JSON import partial-success error display works.

## Automated Run Outcomes (this implementation pass)

### Frontend listening unit/component tests
Command:

```powershell
Set-Location C:\Workspace\FPT\khaleoapp\frontend
npm test -- src/test/listening/dictationEvaluator.test.ts src/test/listening/listeningDictationFlow.test.tsx
```

Outcome: PASS (`2` files, `6` tests passed).

### Frontend listening e2e smoke
Command:

```powershell
Set-Location C:\Workspace\FPT\khaleoapp\frontend
npm run test:e2e -- --grep "listening"
```

Outcome: PASS with SKIP (`1` listening smoke spec detected, currently skipped placeholder).

### Backend listening regression suite
Command:

```powershell
Set-Location C:\Workspace\FPT\khaleoapp\backend
.\mvnw.cmd "-Dtest=LearnerListeningContractTest,LearnerProgressUpdateIT,ListeningFeatureRegressionIT" test
```

Outcome: FAILED in local run due DB connectivity (`Communications link failure` to MySQL).
Action: start/reset local DB and re-run the same command.

