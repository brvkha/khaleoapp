# 01 - System Overview

## Muc tieu

- Xay dung web app hoc tu vung bang flashcard cho nhu cau ca nhan.
- Kien truc de van hanh on dinh voi tai nho (du kien toi da ~5 user dong thoi).
- Trien khai va van hanh don gian, de debug, de rollback.

## Cong nghe chinh

- Frontend: ReactJS.
- Backend: Java Spring Boot.
- Database: MySQL.
- Cloud: AWS.
- Domain: `khaleoshop.click`.

## Moi truong

- Local: Docker Compose (frontend, backend, mysql) cho dev.
- Staging: AWS cau hinh nho, dung test truoc production.
- Production: AWS cho nguoi dung that.

## Nhanh Git de van hanh

- `feature/*`, `bugfix/*`: phat trien tinh nang/sua loi.
- `develop` (hoac `staging`): deploy Staging.
- `main`: deploy Production.

## Nguyen tac kien truc

- Uu tien it thanh phan nhat co the de tiet kiem chi phi.
- Tach biet frontend static, backend API va database.
- Bao mat theo nguyen tac least privilege.
- Tu dong hoa build/deploy bang GitHub Actions.

