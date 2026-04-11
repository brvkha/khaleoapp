# 04 - CI/CD Minimal (GitHub Actions)

## Muc tieu

Tu dong hoa build va deploy cho staging/production voi pipeline gon nhe, de van hanh va rollback.

## Nhanh va trigger

- Push/Merge vao `develop` -> deploy Staging (Terraform + Frontend + Backend).
- Push/Merge vao `main` -> deploy Production.

## Terraform workflow (staging)

1. Checkout code.
2. Setup Terraform.
3. Apply `bootstrap/` (S3 state bucket + DynamoDB lock).
4. Init `app/` voi remote backend staging.
5. Plan + apply stack staging.
6. Ho tro `workflow_dispatch` voi `action=apply|destroy|recreate` de test destroy/recreate reproducibility.

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
6. SSM vao EC2, pull image moi, restart container (khong dung SSH key).

## Secrets toi thieu tren GitHub

- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `TF_STATE_BUCKET_STAGING`
- `TF_LOCK_TABLE_STAGING` (optional, mac dinh `khaleoapp-terraform-lock`)
- `TF_BACKEND_KEY_STAGING` (optional, mac dinh `khaleoapp/staging/terraform.tfstate`)
- `S3_BUCKET_STAGING`, `S3_BUCKET_PROD`
- `CF_DIST_ID_STAGING`, `CF_DIST_ID_PROD`
- `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`
- `EC2_INSTANCE_ID_STAGING` (optional fallback tu tags)
- `RDS_DB_NAME`, `RDS_DB_USER`, `RDS_DB_PASSWORD`
- `JWT_SECRET_STAGING`, `TLS_EMAIL_STAGING` (optional)
- `VITE_API_BASE_URL_STAGING` (optional, fallback `https://api-staging.khaleoshop.click`)

## Deployment strategy

- Moi truong dung file env rieng.
- Truoc khi restart backend, backup log va giu lai image version truoc de rollback nhanh.
- Dat image tag theo commit SHA de truy vet.

## Checklist van hanh

- Flyway migration chay thanh cong khi app boot.
- Health check API pass sau deploy.
- Dang nhap/refresh/logout test nhanh sau moi lan release.

