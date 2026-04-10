# 09 - CI/CD Workflows (GitHub Actions)

## Strategy

### Branch Strategy
- `develop` → Staging environment
- `main` → Production environment

### Pipeline Strategy
- **Frontend:** Build React → Sync S3 → CloudFront invalidate
- **Backend:** Build JAR → Docker image → Push Hub → SSM Run Command → Container restart
- Tự động trigger khi push to branch
- Parallelizable: FE & BE có thể chạy đồng thời nếu file không overlap

---

## Frontend Workflow (`.github/workflows/deploy-frontend.yml`)

### Trigger
```
on push to develop | main
  paths:
    - frontend/**
    - .github/workflows/deploy-frontend.yml
```

### Steps
1. **Checkout code**
2. **Setup Node.js 18** (cache npm)
3. **Install dependencies** (`npm ci`)
4. **Build** (`npm run build`)
5. **AWS credentials** (access key + secret key)
6. **Sync dist/ to S3**
   - Branch `develop` → `S3_BUCKET_STAGING`
   - Branch `main` → `S3_BUCKET_PROD`
   - Command: `aws s3 sync dist/ s3://{bucket} --delete`
7. **CloudFront invalidate**
   - Branch `develop` → `CF_DIST_ID_STAGING`
   - Branch `main` → `CF_DIST_ID_PROD`
   - Command: `aws cloudfront create-invalidation --distribution-id {id} --paths "/*"`

### Outputs
- ✅ Từng bước build log in GitHub Actions
- ✅ S3 bucket được cập nhật file mới
- ✅ CloudFront cache bị xóa → người dùng nhận bản mới lập tức

### Secrets Required
```
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
S3_BUCKET_STAGING
S3_BUCKET_PROD
CF_DIST_ID_STAGING
CF_DIST_ID_PROD
```

---

## Backend Workflow (`.github/workflows/deploy-backend.yml`)

### Trigger
```
on push to develop | main
  paths:
    - backend/**
    - .github/workflows/deploy-backend.yml
```

### Steps

#### Phase 1: Build
1. **Checkout code**
2. **Setup JDK 17** (cache Maven)
3. **Build JAR** (`./mvnw clean package -DskipTests`)
   - Output: `target/flashcard-backend-0.1.0-SNAPSHOT.jar`

#### Phase 2: Docker
4. **Setup Docker Buildx** (multi-platform support)
5. **Login Docker Hub** (username + token)
6. **Build & Push image**
   - Tag `khaleo/backend:latest`
   - Tag `khaleo/backend:{github.sha}` (7 ký tự commit SHA)
   - Cache layer để lần sau build nhanh hơn

#### Phase 3: Deploy
7. **Resolve EC2 instance id**
   - Branch `develop` ưu tiên `EC2_INSTANCE_ID_STAGING` hoặc fallback query theo tags `Project` + `Environment`
8. **Run remote deploy script via AWS SSM** (`aws ssm send-command`)
8. **On EC2, run script:**
   ```bash
   # Pull image mới nhất
   docker pull khaleo/backend:{github.sha}
   
   # Stop/remove container cũ
   docker stop khaleo-backend || true
   docker rm khaleo-backend || true
   
   # Run container mới với env vars
   docker run -d \
     --name khaleo-backend \
     -p 8080:8080 \
     -e SPRING_PROFILES_ACTIVE=prod|staging \
     -e DB_HOST={RDS_HOST} \
     -e DB_NAME={RDS_DB_NAME} \
     -e DB_USER={RDS_DB_USER} \
     -e DB_PASSWORD={RDS_DB_PASSWORD} \
     -e JWT_SECRET={JWT_SECRET} \
     khaleo/backend:{github.sha}
   ```

### Outputs
- ✅ JAR compiled, tested skipped (tối ưu tốc độ)
- ✅ Docker image pushed to Hub
- ✅ New container running on EC2
- ✅ Service available ở `http://api.{domain}` (thông qua Nginx)

### Secrets Required
```
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
DOCKERHUB_USERNAME
DOCKERHUB_TOKEN
EC2_INSTANCE_ID_STAGING (optional but recommended)
RDS_HOST_PROD
RDS_HOST_STAGING
RDS_DB_NAME
RDS_DB_USER
RDS_DB_PASSWORD
JWT_SECRET_STAGING
```

---

## Environment-Specific Config

### Staging (develop branch)
- **Frontend:** `https://staging.khaleoshop.click`
- **Backend:** `https://api-staging.khaleoshop.click`
- **Database:** RDS Staging instance
- **JWT_SECRET:** Staging secret

### Production (main branch)
- **Frontend:** `https://khaleoshop.click`
- **Backend:** `https://api.khaleoshop.click`
- **Database:** RDS Prod instance
- **JWT_SECRET:** Prod secret (khác staging)

---

## Flow Diagram

```
Developer Push Code
  ↓
GitHub Actions Triggered
  ├─ Frontend: checkout → build → S3 sync → CF invalidate
  └─ Backend: checkout → build JAR → Docker build → Docker push → SSM send-command → docker run
  ↓
Both Status: ✅ Success / ❌ Failure → GitHub notification
  ↓
Staging: User can test
  ↓
If OK, merge to main → Deploy Production
```

---

## Rollback Strategy

### Frontend
- Nếu build fail: không up S3
- Nếu muốn rollback: push commit cũ hoặc tạo hotfix branch

### Backend
- Container mới fail: giữ lại image cũ (lệnh stop/rm chỉ xóa container)
- Rollback: chạy lại workflow với commit/tag cũ hoặc gửi SSM command để `docker run ... old-image-id`

---

## Monitoring & Alerts

Setup trong GitHub Actions:
- ✅ Log chi tiết mỗi step
- ✅ Notify Slack/Email khi fail
- ✅ Generate artifact (JAR, build reports)

---

## Checklist trước Deploy

```
[ ] AWS credentials setup trong GitHub Secrets
[ ] Docker Hub account + token setup
[ ] EC2 instance managed bởi SSM (instance profile + SSM agent online)
[ ] S3 buckets created (staging + prod)
[ ] CloudFront distributions configured
[ ] Route53 DNS pointing to CloudFront/EC2
[ ] RDS databases created (staging + prod)
[ ] JWT_SECRET generated (khác nhau staging/prod)
[ ] .github/workflows/ folder created trong repo
[ ] YAML workflows reviewed syntax
[ ] Test run: push to `develop` branch → watch Actions log
```

