#!/usr/bin/env bash
set -euo pipefail

# Fetch runtime secrets and materialize runtime env properties for Spring Boot.
DB_SECRET_JSON=$(aws secretsmanager get-secret-value --secret-id "${DB_SECRET_ID}" --query SecretString --output text)
JWT_SECRET_JSON=$(aws secretsmanager get-secret-value --secret-id "${JWT_SECRET_ID}" --query SecretString --output text)

DB_HOST=$(echo "$DB_SECRET_JSON" | jq -r '.host')
DB_PORT=$(echo "$DB_SECRET_JSON" | jq -r '.port // 3306')
DB_NAME=$(echo "$DB_SECRET_JSON" | jq -r '.dbname // .database // "khaleoapp"')
DB_USER=$(echo "$DB_SECRET_JSON" | jq -r '.username')
DB_PASS=$(echo "$DB_SECRET_JSON" | jq -r '.password')
JWT_SECRET_VALUE=$(echo "$JWT_SECRET_JSON" | jq -r '.secret // .jwt_secret // .value')

RUNTIME_FILE="${RUNTIME_ENV_PATH:-/opt/khaleo/flashcard-backend/runtime-secrets.env}"
mkdir -p "$(dirname "$RUNTIME_FILE")"

cat > "$RUNTIME_FILE" <<EOF
DB_URL=jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=${DB_USER}
DB_PASSWORD=${DB_PASS}
JWT_SECRET=${JWT_SECRET_VALUE}
EOF

echo "Runtime env generated at ${RUNTIME_FILE}"

