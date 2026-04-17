# 03 - Auth & Security Policy (Spring Security + JWT)

## Pham vi

Tai lieu nay chot chinh sach xac thuc va phan quyen cho KhaLeo.

## Quyet dinh da khoa

- Framework bao mat: Spring Security.
- Co che token: JWT.
- `access token` TTL: 15 phut.
- `refresh token` TTL: 7 ngay.
- Luu `refresh token` trong DB bang bang `refresh_tokens` (cot `token`, `expires_at`, `revoked_at`).
- Refresh token hien tai khong rotate khi goi `/auth/refresh` (chi cap moi access token).
- Dang nhap sai 5 lan -> khoa dang nhap 15 phut.
- Khong `remember me`.
- Cho phep nhieu phien (multi-session), moi refresh token duoc quan ly doc lap.
- Vai tro: `USER`, `ADMIN`.
- Login identifier: chap nhan `username` hoac `email` (mot trong hai).
- Dang ky bat buoc `username` + `password`; `email` la optional.
- Phase hien tai khong bat buoc email verification va khong mo forgot/reset password endpoint.

## UX/Authorization behavior

- Dang nhap thanh cong: chuyen vao trang `home`.
- Neu role la `ADMIN`: frontend hien thi them tab `admin`.
- Endpoint quan tri bat buoc role `ADMIN`.

## Luong token de xuat

1. User dang nhap thanh cong -> cap `access token` + `refresh token`.
2. Backend luu refresh token vao bang `refresh_tokens`.
3. Khi access token het han, frontend goi endpoint refresh.
4. Backend verify refresh token hop le + con han + chua bi revoke.
5. Backend cap access token moi (refresh token giu nguyen).
6. Logout theo refresh token se danh dau `revoked_at` de vo hieu hoa token do.

## Luu tru va truyen token

- Uu tien:
  - `access token` gui theo `Authorization: Bearer <token>`.
  - `refresh token` truyen trong body JSON cho `/auth/refresh` va `/auth/logout`.
- CORS allowlist theo moi truong, khong mo `*` khi co credentials.

## JWT Claims

- Access token (login + refresh) gom cac claim:
  - `sub`: userId
  - `role`: `ROLE_USER` | `ROLE_ADMIN`
  - `username`: claim chinh
  - `email`: claim transitional (chi co khi user co email)

## Cac endpoint auth toi thieu

- `POST /api/v1/auth/register` (body: `{ username, email?, password }`)
- `POST /api/v1/auth/login` (body: `{ identifier, password }`)
- `POST /api/v1/auth/refresh` (body: `{ refreshToken }`)
- `POST /api/v1/auth/logout` (body: `{ refreshToken }`)

Khong co endpoint verify email, forgot password, reset password o phase hien tai.

## Du lieu DB toi thieu (goi y)

Bang `refresh_tokens`:

- `id`
- `user_id`
- `token`
- `expires_at`
- `revoked_at` (timestamp nullable)
- `created_at`
- `updated_at`

Luu y: can index theo `user_id` va token active (`revoked_at is null`, `expires_at > now`).

## Secret management

- Local: bien moi truong tu file `.env`.
- Staging/Prod: luu secret tren AWS Secrets Manager.
- Khong hard-code JWT secret trong source code.

## Audit va logging

- Log cac su kien: login success/fail, refresh, logout, token reuse.
- Khong log plaintext token hoac password.
- Co the luu them `ip`, `user_agent` de truy vet co ban.

