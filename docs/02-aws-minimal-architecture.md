# 02 - AWS Minimal Architecture

## Muc tieu ha tang

Thiet ke toi gian chi phi cho app ca nhan, van dam bao co staging va production, co SSL, co backup DB, va de mo rong sau nay neu can.

## So do thanh phan (Production)

- Route 53 (`khaleoshop.click`, `api.khaleoshop.click`).
- CloudFront + S3 cho frontend React static files.
- EC2 (t3.micro) chay Backend Spring Boot trong Docker.
- Nginx tren EC2 lam reverse proxy + SSL termination.
- RDS MySQL (db.t3.micro) dat private subnet.

## Luong request

1. User vao `khaleoshop.click` -> Route 53 -> CloudFront -> S3 (frontend).
2. Frontend goi API `api.khaleoshop.click` -> Route 53 -> Elastic IP cua EC2 -> Nginx -> Spring Boot container (`:8080`).
3. Backend truy cap RDS MySQL trong private subnet.

## Networking va bao mat toi thieu

- EC2 dat public subnet, gan Elastic IP.
- RDS dat private subnet, `Publicly Accessible = No`.
- Security Group cho EC2:
  - Mo `80`, `443` cho web traffic.
  - Mo `22` chi tu IP tin cay.
- Security Group cho RDS:
  - Mo `3306` chi tu Security Group cua EC2 backend.

## Staging

Dung mo hinh y het production nhung cau hinh nho hon va domain/subdomain rieng de test.

## Cost-first notes

- Khong dung ALB/ECS/K8s giai doan dau.
- 1 EC2 + 1 RDS la du cho tai nho.
- Bat monitor co ban (CPU, RAM, disk, DB connections) de biet thoi diem can nang cap.

