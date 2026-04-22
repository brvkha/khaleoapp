# KhaLeo Docs

Tai lieu nay chot pham vi kien truc va quy trinh trien khai cho KhaLeo theo huong toi gian chi phi, phu hop app ca nhan.

## Thu tu doc de trien khai

1. `docs/01-system-overview.md` - Tổng quan hệ thống
2. `docs/02-aws-minimal-architecture.md` - Kiến trúc AWS tối giản
3. `docs/03-auth-security-policy.md` - JWT + Spring Security
4. `docs/04-cicd-minimal.md` - Quy trình tự động hóa
5. `docs/05-terraform-infra-plan.md` - Provision AWS bằng Terraform
6. `docs/06-frontend-design.md` - Thiết kế React (3 tabs)
7. `docs/07-backend-design.md` - Thiết kế Spring Boot + FSRS v6
8. `docs/08-database-schema.md` - Schema Flyway
9. `docs/09-cicd-workflows.md` - GitHub Actions workflows
10. `docs/10-frontend-backend-integration-spec.md` - API contract + state management
11. `docs/11-implementation-plan.md` - Roadmap 4 phases
12. `docs/12-tasks.md` ⭐ **93 TASKS chi tiết (dependency-ordered)**

## Quyet dinh da khoa

- Moi truong: Local, Staging, Production.
- AWS toi gian cho tai 5 user dong thoi.
- Auth: Spring Security + JWT (`access token` 15 phut, `refresh token` 7 ngay).
- Refresh token luu DB dang hash, co rotate token.
- Neu phat hien refresh token cu bi dung lai: revoke phien hien tai.
- Dang nhap sai 5 lan: khoa dang nhap 15 phut.
- Role: `USER`, `ADMIN`.
- Region AWS: `ap-southeast-1`.
- Terraform backend: S3 + DynamoDB lock.
- Truy cap EC2 bang SSM Session Manager.
- Frontend: React + Tailwind, 3 tabs (Decks, Cards, Study).
- Backend: Spring Boot + FSRS v6 (w0..w18), desired_retention=0.9.
- CI/CD: GitHub Actions, `develop` → Staging, `main` → Production.

## Constitution compliance (bat buoc)

- Tu 2026-04-22, moi ke hoach/spec/task phai tuan thu `.specify/memory/constitution.md` v1.0.0.
- Gate merge/release toi thieu: lint + test + build xanh, khong vo gate bao mat media/dictionary,
  va co bang chung budget hieu nang khi tinh nang bi anh huong.

## Out of scope giai doan nay

- Microservices, Kubernetes, ALB autoscaling phuc tap.
- Multi-device session management.
- SSO/OAuth social login.
