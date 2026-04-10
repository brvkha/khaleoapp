# 03 - Auth & Security Policy (Spring Security + JWT)

## Pham vi

Tai lieu nay chot chinh sach xac thuc va phan quyen cho KhaLeo.

## Quyet dinh da khoa

- Framework bao mat: Spring Security.
- Co che token: JWT.
- `access token` TTL: 15 phut.
- `refresh token` TTL: 7 ngay.
- Luu `refresh token` trong DB o dang hash (khong luu plaintext).
- Co `refresh token rotation` moi lan refresh.
- Neu phat hien refresh token cu duoc tai su dung sau rotate: revoke phien hien tai.
- Dang nhap sai 5 lan -> khoa dang nhap 15 phut.
- Khong `remember me`.
- Khong `multi-device login` (moi user mot phien hop le tai mot thoi diem).
- Vai tro: `USER`, `ADMIN`.

## UX/Authorization behavior

- Dang nhap thanh cong: chuyen vao trang `home`.
- Neu role la `ADMIN`: frontend hien thi them tab `admin`.
- Endpoint quan tri bat buoc role `ADMIN`.

## Luong token de xuat

1. User dang nhap thanh cong -> cap `access token` + `refresh token`.
2. Backend luu hash refresh token vao bang session/token.
3. Khi access token het han, frontend goi endpoint refresh.
4. Backend verify refresh token hop le + con han + chua bi revoke.
5. Backend rotate: tao refresh token moi, vo hieu hoa token cu, cap access token moi.
6. Neu token cu bi dung lai sau rotate -> revoke phien hien tai va yeu cau dang nhap lai.

## Luu tru va truyen token

- Uu tien:
  - `access token` gui theo `Authorization: Bearer <token>`.
  - `refresh token` luu o kenh an toan hon (cookie HttpOnly/Secure neu frontend-backend cung chinh sach domain phu hop).
- CORS allowlist theo moi truong, khong mo `*` khi co credentials.

## Cac endpoint auth toi thieu

- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `GET /api/auth/me`

## Du lieu DB toi thieu (goi y)

Bang `user_sessions` (hoac ten tuong duong):

- `id`
- `user_id`
- `refresh_token_hash`
- `expires_at`
- `revoked` (boolean)
- `created_at`
- `updated_at`

Luu y: can index theo `user_id` va cot tim kiem phien dang hoat dong.

## Secret management

- Local: bien moi truong tu file `.env`.
- Staging/Prod: luu secret tren AWS Secrets Manager.
- Khong hard-code JWT secret trong source code.

## Audit va logging

- Log cac su kien: login success/fail, refresh, logout, token reuse.
- Khong log plaintext token hoac password.
- Co the luu them `ip`, `user_agent` de truy vet co ban.

