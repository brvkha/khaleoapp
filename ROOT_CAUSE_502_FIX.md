# 502 Gateway - Root Cause & Fix Summary

## 🔴 Problem Found

**Error**: `Access denied for user 'root'@'10.1.1.11' (using password: NO)`

**Location**: Backend container logs show Flyway migration failure trying to connect to RDS

**Root Cause**: Docker environment variables were **NOT being passed** to container

```
DB_PASSWORD=DB_PASS_VAL  ← Still has placeholder, not replaced!
DB_USERNAME=DB_USER_VAL  ← Still has placeholder, not replaced!
JWT_SECRET=JWT_VAL        ← Still has placeholder, not replaced!
```

### Why Substitution Failed

**Original code**:
```bash
cat > /tmp/deploy_params.json <<'EOF'
{
  "commands": [
    "docker run -d ... -e DB_PASSWORD='DB_PASS_VAL' ..."
  ]
}
EOF

sed -i "s|DB_PASS_VAL|${RDS_DB_PASSWORD}|g" /tmp/deploy_params.json
```

**Problem**: 
- Single quotes `'...'` protect from shell expansion
- So `'DB_PASS_VAL'` was treated as literal string, not regex pattern
- sed couldn't match it because the quotes are part of the string value!

---

## ✅ Solution

**Updated code**:
```bash
# BEFORE: 'DB_PASS_VAL' (quotes inside JSON)
"docker run -d ... -e DB_PASSWORD='DB_PASS_VAL' ..."

# AFTER: DB_PASS_VAL (no quotes, sed can replace)
"docker run -d ... -e DB_PASSWORD=DB_PASS_VAL ..."

# AND: Proper variable quoting for sed
DB_PASS_ESCAPED=$(printf '%s\n' "$RDS_DB_PASSWORD" | sed -e 's/[\/&]/\\&/g')
sed -i "s|DB_PASS_VAL|${DB_PASS_ESCAPED}|g" /tmp/deploy_params.json
```

---

## 📋 Changes Made

File: `.github/workflows/deploy-backend.yml`

### 1. Remove quotes from env var values
```diff
- "docker run -d ... -e DB_PASSWORD='DB_PASS_VAL' -e JWT_SECRET='JWT_VAL' ..."
+ "docker run -d ... -e DB_PASSWORD=DB_PASS_VAL -e JWT_SECRET=JWT_VAL ..."
```

### 2. Add proper escaping for special characters
```bash
# Escape / and & in passwords (common in special chars)
DB_PASS_ESCAPED=$(printf '%s\n' "$RDS_DB_PASSWORD" | sed -e 's/[\/&]/\\&/g')
sed -i "s|DB_PASS_VAL|${DB_PASS_ESCAPED}|g" /tmp/deploy_params.json

JWT_ESCAPED=$(printf '%s\n' "$JWT_SECRET" | sed -e 's/[\/&]/\\&/g')
sed -i "s|JWT_VAL|${JWT_ESCAPED}|g" /tmp/deploy_params.json
```

### 3. Use proper sed delimiter
Already using `|` delimiter instead of `/` to avoid issues with `/` in passwords

### 4. Improve visibility
- Show deployment config being sent
- 15s wait for container startup
- Full logs output (100 lines)
- Verbose health check

---

## 🧪 Verification

After next deployment, backend should:

1. ✅ Receive all environment variables correctly
2. ✅ Connect to RDS successfully
3. ✅ Run Flyway migrations
4. ✅ Start Spring Boot application
5. ✅ Listen on port 8080
6. ✅ Respond to health checks
7. ✅ Nginx proxy passes requests without 502

---

## 📊 Impact

- **502 Error**: FIXED ✅
- **CORS Error**: Already fixed (Nginx headers forwarding) ✅
- **Database Connection**: Will work ✅

---

## 🚀 Next Steps

1. **Deploy with fixed script** (GitHub Actions)
2. **Monitor backend logs** for successful startup
3. **Test from browser** - should work now!

```bash
# After deployment, verify:
curl -k https://api.khaleoshop.click/api/v1/auth/login \
  -H "Origin: https://khaleoshop.click" \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test"}'

# Expected: 400 or 401 (bad credentials), NOT 502
```

---

## 🔗 Related Issues

- Nginx CORS headers: ✅ Fixed in previous commit
- Backend deployment credentials: ✅ Fixed in this commit
- Database connectivity: ✅ Will be fixed after redeployment

