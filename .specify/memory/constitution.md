<!--
Sync Impact Report
- Version change: N/A (template) → 1.0.0
- Modified principles:
  - [PRINCIPLE_1_NAME] → I. Monorepo Contract Integrity (Spring Boot + React)
  - [PRINCIPLE_2_NAME] → II. Security First for Auth, Media, and Dictionary Access
  - [PRINCIPLE_3_NAME] → III. Test Gates Are Non-Negotiable
  - [PRINCIPLE_4_NAME] → IV. Performance Budgets Must Be Measured and Enforced
  - [PRINCIPLE_5_NAME] → V. Observability, Operability, and Minimal Complexity
- Added sections:
  - Technology & Delivery Constraints
  - Workflow, Review, and Release Gates
- Removed sections:
  - None
- Templates requiring updates:
  - ✅ updated: .specify/templates/plan-template.md
  - ✅ updated: .specify/templates/spec-template.md
  - ✅ updated: .specify/templates/tasks-template.md
  - ✅ updated: docs/README.md
  - ⚠ pending: .specify/templates/commands/*.md (directory not present)
- Deferred TODOs:
  - None
-->

# khaleoapp Constitution

## Core Principles

### I. Monorepo Contract Integrity (Spring Boot + React)
All changes MUST preserve explicit API contracts between `backend/` (Spring Boot) and
`frontend/` (React). Contract-breaking changes MUST ship with synchronized backend + frontend
updates in one feature branch and updated contract tests. Shared DTO/field semantics MUST be
versioned in specs before implementation. Rationale: this monorepo fails at runtime when either
side drifts.

### II. Security First for Auth, Media, and Dictionary Access
Protected endpoints MUST require authenticated access and role checks by least privilege. Media
access MUST use time-limited signed URLs; raw private object paths MUST NOT be exposed. Dictionary
integrations MUST use allowlisted upstream hosts, request timeouts, input sanitization, and
cache/response controls to prevent abuse. Secrets MUST come from environment or secret managers,
never source code. Security events MUST be logged without plaintext tokens/passwords. Rationale:
media and dictionary flows are high-risk external boundaries.

### III. Test Gates Are Non-Negotiable
Every feature PR MUST include automated tests for affected logic: unit tests plus integration or
contract tests for API/state boundaries. Regressions in auth, media upload/access, dictionary, and
listening flows MUST include dedicated coverage. CI MUST fail on lint/test/build failure; merges to
`develop`/`main` are blocked until green. Test fixes without assertions are invalid. Rationale:
quality must be enforced by gates, not manual intent.

### IV. Performance Budgets Must Be Measured and Enforced
Backend APIs for interactive flows SHOULD maintain p95 latency ≤ 300ms in normal load; auth and
media access token issuance SHOULD maintain p95 ≤ 500ms. Frontend initial route render for core
screens SHOULD complete within 2.5s on standard broadband test profile. Bundle additions per feature
SHOULD be justified when exceeding +150KB gzipped JS. Any budget violation MUST include a measured
report and approved mitigation plan in the PR. Rationale: small-user systems still degrade quickly
without explicit budgets.

### V. Observability, Operability, and Minimal Complexity
Structured logs, correlation identifiers, and actionable error messages MUST exist for critical user
journeys (auth, deck study, listening, media, dictionary). Health checks and migration status MUST be
verifiable before release. Teams SHOULD prefer the simplest architecture that satisfies current scope;
new infrastructure tiers require documented operational need. Rationale: operability and simplicity
reduce incident time and cost.

## Technology & Delivery Constraints

- Backend MUST remain Spring Boot with managed dependency versions and Flyway migrations.
- Frontend MUST remain React with typed API service boundaries and lint-enforced code standards.
- Repository structure MUST keep `backend/` and `frontend/` independently buildable and testable.
- Infrastructure or CI changes MUST preserve staged promotion (`develop` → staging, `main` →
  production) and rollback capability.
- User-facing security defaults (JWT TTL, refresh handling, lockout, CORS policy) MUST be documented
  when changed.

## Workflow, Review, and Release Gates

- Plans MUST pass Constitution Check before research/design and before implementation.
- Specs MUST state security impact, required test layers, and measurable performance expectations.
- Tasks MUST include explicit security, testing, and performance-verification work items.
- PR review MUST verify: contract compatibility, required tests, performance evidence, and migration
  safety.
- Release approval MUST confirm green CI, migration success, and no unresolved critical security
  findings.

## Governance

This constitution is the highest-priority engineering policy for khaleoapp. If any document or local
practice conflicts, this constitution prevails.

Amendments MUST be proposed via PR that includes: (1) rationale, (2) impacted templates/docs, and
(3) migration or adoption steps. Approval requires at least one backend and one frontend maintainer.

Versioning policy uses semantic versioning for this document: MAJOR for incompatible governance
changes or principle removals, MINOR for new principles/sections or materially stronger obligations,
PATCH for clarifications/wording that do not change obligations.

Compliance review is required in planning, PR review, and release checklists. Non-compliance MUST be
documented with time-bound remediation.

**Version**: 1.0.0 | **Ratified**: 2026-04-22 | **Last Amended**: 2026-04-22
