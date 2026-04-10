# KhaLeo Docs

Tai lieu nay chot pham vi kien truc va quy trinh trien khai cho KhaLeo theo huong toi gian chi phi, phu hop app ca nhan.

## Thu tu doc de trien khai

1. `docs/01-system-overview.md`
2. `docs/02-aws-minimal-architecture.md`
3. `docs/03-auth-security-policy.md`
4. `docs/04-cicd-minimal.md`

## Quyet dinh da khoa

- Moi truong: Local, Staging, Production.
- AWS toi gian cho tai 5 user dong thoi.
- Auth: Spring Security + JWT (`access token` 15 phut, `refresh token` 7 ngay).
- Refresh token luu DB dang hash, co rotate token.
- Neu phat hien refresh token cu bi dung lai: revoke phien hien tai.
- Dang nhap sai 5 lan: khoa dang nhap 15 phut.
- Role: `USER`, `ADMIN`.

## Out of scope giai doan nay

- Microservices, Kubernetes, ALB autoscaling phuc tap.
- Multi-device session management.
- SSO/OAuth social login.

