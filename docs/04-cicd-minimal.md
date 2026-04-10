# 04 - CI/CD Minimal (GitHub Actions)

## Muc tieu

Tu dong hoa build va deploy cho staging/production voi pipeline gon nhe, de van hanh va rollback.

## Nhanh va trigger

- Push/Merge vao `develop` -> deploy Staging.
- Push/Merge vao `main` -> deploy Production.

## Frontend workflow

1. Checkout code.
2. Setup Node.js.
3. Cai dependencies (`npm ci`).
4. Build (`npm run build`).
5. Sync thu muc build len S3 bucket moi truong.
6. CloudFront invalidation de cap nhat ban moi.

## Backend workflow

1. Checkout code.
2. Setup JDK 17.
3. Build jar (`./mvnw clean package -DskipTests`).
4. Build Docker image.
5. Push image len Docker Hub.
6. SSH vao EC2, pull image moi, restart container.

## Secrets toi thieu tren GitHub

- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_REGION`
- `S3_BUCKET_STAGING`, `S3_BUCKET_PROD`
- `CLOUDFRONT_DIST_ID_STAGING`, `CLOUDFRONT_DIST_ID_PROD`
- `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`
- `EC2_HOST_STAGING`, `EC2_HOST_PROD`
- `EC2_SSH_USER`
- `EC2_SSH_PRIVATE_KEY`

## Deployment strategy

- Moi truong dung file env rieng.
- Truoc khi restart backend, backup log va giu lai image version truoc de rollback nhanh.
- Dat image tag theo commit SHA de truy vet.

## Checklist van hanh

- Flyway migration chay thanh cong khi app boot.
- Health check API pass sau deploy.
- Dang nhap/refresh/logout test nhanh sau moi lan release.

