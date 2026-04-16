# KhaLeo App

Monorepo cho KhaLeo flashcard app:
- `backend/`: Spring Boot + MySQL + Flyway + JWT
- `frontend/`: React + Vite + TypeScript + Tailwind
- `infra/terraform/`: Terraform cho prod
- `docs/`: đặc tả và kế hoạch triển khai

## Phase 1 Local Development

### Prerequisites
- Java 17+
- Maven 3.9+
- Node.js 20+
- Docker Desktop

### 1) Start local MySQL

```powershell
docker compose up -d
```

MySQL local chạy tại `localhost:3306`, DB mặc định: `khaleoapp`.

### 2) Run backend

```powershell
Set-Location .\backend
mvn spring-boot:run
```

Backend chạy tại `http://localhost:8080`.
Flyway tự migrate schema và seed user local:
- email: `user@test.com`
- password: `password123`

### 3) Run frontend

```powershell
Set-Location .\frontend
npm install
npm run dev
```

Frontend chạy tại `http://localhost:5173` (Vite proxy `/api` sang backend).

## Useful test/build commands

```powershell
Set-Location .\backend
mvn test -Dtest=JwtTokenServiceTest,SpacedRepetitionServiceTest
mvn -DskipTests package
```

```powershell
Set-Location .\frontend
npm run test
npm run build
```
