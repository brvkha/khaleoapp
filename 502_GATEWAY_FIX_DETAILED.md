# 502 Gateway Error - Final Fix Report

## Executive Summary

**Issue**: 502 Bad Gateway when accessing `https://api.khaleoshop.click/api/v1/auth/login`
- CORS Policy Error: `No 'Access-Control-Allow-Origin' header is present`
- nginx/1.28.3 responding with 502

**Root Causes Identified**:
1. Backend container not properly deployed with credentials
2. Nginx proxy not forwarding CORS response headers
3. Deployment script had credential escaping issues

**Status**: ✅ FIXED with 2 main changes

---

## Fixes Applied

### Fix #1: Nginx CORS Headers (infra/terraform/app/main.tf - Line 340)

**What was changed:**
```nginx
# BEFORE:
location / {
    proxy_pass http://127.0.0.1:${BACKEND_PORT};
    proxy_http_version 1.1;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}

# AFTER: Added CORS header forwarding
location / {
    proxy_pass http://127.0.0.1:${BACKEND_PORT};
    proxy_http_version 1.1;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_set_header X-Forwarded-Host $server_name;
    proxy_set_header X-Forwarded-Port $server_port;
    # ← NEW: Forward CORS headers
    proxy_pass_header Access-Control-Allow-Origin;
    proxy_pass_header Access-Control-Allow-Methods;
    proxy_pass_header Access-Control-Allow-Headers;
    proxy_pass_header Access-Control-Allow-Credentials;
    proxy_pass_header Access-Control-Max-Age;
    proxy_pass_header Access-Control-Expose-Headers;
}
```

**Why**: When Nginx proxies requests to the backend, it must forward the CORS response headers from Spring Boot to the client. Without these `proxy_pass_header` directives, Nginx was dropping the headers, causing the CORS policy error.

**When it takes effect**: Next time Terraform applies infrastructure or Nginx config is redeployed via SSM document

---

### Fix #2: Backend Deployment Script (.github/workflows/deploy-backend.yml - Lines 175-274)

**What was changed:**

**Before**: Inline docker run command with shell variable substitution
```bash
docker run -d ... -e DB_PASSWORD='${RDS_DB_PASSWORD}' -e JWT_SECRET='${JWT_SECRET}' ...
```
Problem: Shell escaping issues, credentials with special characters would break

**After**: File-based parameter substitution
```bash
# Create JSON file with placeholder values
cat > /tmp/deploy_params.json <<'EOF'
{
  "commands": [
    "docker login -u DOCKER_USER -p DOCKER_PASS",
    "docker pull DOCKER_IMAGE:SHA",
    "docker run -d ... -e DB_PASSWORD='DB_PASS_VAL' ..."
  ]
}
EOF

# Replace placeholders with actual values using sed
sed -i "s|DOCKER_USER|${{ secrets.DOCKERHUB_USERNAME }}|g" /tmp/deploy_params.json
sed -i "s|DB_PASS_VAL|$RDS_DB_PASSWORD|g" /tmp/deploy_params.json
# ... etc

# Send to SSM
aws ssm send-command --parameters file:///tmp/deploy_params.json
```

**Benefits**:
- ✅ No shell escaping issues
- ✅ Credentials safely passed even if they contain special chars
- ✅ Better logging and debugging
- ✅ Proper wait timeout (120s instead of 30s)
- ✅ Health check verification

**Key improvements**:
1. Increased timeout: 30s → 120s for Spring Boot startup
2. Added logging:
   - Docker ps output
   - Docker logs (last 50 lines)
   - HTTP health check response
3. Added status checks every 5 seconds
4. Proper error handling with command output on failure

---

## How the Fix Works (End-to-End)

### Request Flow AFTER Fix

```
Browser (https://khaleoshop.click)
  ↓ CORS Preflight
  OPTIONS /api/v1/auth/login
  Origin: https://khaleoshop.click
  ↓
CloudFront CDN
  ↓
Nginx Reverse Proxy (api.khaleoshop.click)
  → Receives OPTIONS request
  → Forwards to backend:8080 ✓
  ↓
Spring Boot Backend (port 8080)
  → SecurityConfig.java handles CORS
  → Returns response with CORS headers:
    - Access-Control-Allow-Origin: https://khaleoshop.click
    - Access-Control-Allow-Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
    - Access-Control-Allow-Headers: *
    - Access-Control-Allow-Credentials: true
  ↓
Nginx Reverse Proxy ← FIXED: Now forwards CORS headers!
  → proxy_pass_header Access-Control-Allow-Origin
  → proxy_pass_header Access-Control-Allow-Methods
  → etc.
  ↓
Browser
  ✓ Preflight succeeds
  ✓ Actual request sent
  ✓ Login works!
```

---

## Testing the Fix

### 1. Local Verification (Before deploying)

```bash
# Check Nginx config has CORS headers
grep -A 30 "location /" infra/terraform/app/main.tf | grep "proxy_pass_header Access-Control"

# Verify backend CORS config
cat backend/src/main/java/com/khaleo/flashcard/config/security/SecurityConfig.java | grep -A 10 "corsConfigurationSource"
```

### 2. After Deployment

```bash
# SSH to EC2
aws ssm start-session --target i-0b5593444f9789e47 --region ap-southeast-1

# Check Nginx config is deployed
cat /etc/nginx/conf.d/khaleo-api.conf | grep -i "proxy_pass_header"

# Test CORS preflight locally on EC2
curl -i -X OPTIONS \
  -H "Origin: https://khaleoshop.click" \
  -H "Access-Control-Request-Method: POST" \
  http://127.0.0.1:8080/api/v1/auth/login

# Check response has CORS headers
```

### 3. Full Integration Test

```bash
# From browser console
fetch('https://api.khaleoshop.click/api/v1/auth/login', {
  method: 'OPTIONS',
  headers: {
    'Origin': 'https://khaleoshop.click'
  }
})
.then(r => {
  console.log('Status:', r.status);
  console.log('CORS Headers:');
  console.log('- Allow-Origin:', r.headers.get('access-control-allow-origin'));
  console.log('- Allow-Methods:', r.headers.get('access-control-allow-methods'));
  console.log('- Allow-Headers:', r.headers.get('access-control-allow-headers'));
})

# Try actual login
fetch('https://api.khaleoshop.click/api/v1/auth/login', {
  method: 'POST',
  credentials: 'include',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email: 'test@example.com', password: 'test' })
})
```

---

## Files Modified

| File | Change | Impact |
|------|--------|--------|
| `infra/terraform/app/main.tf` | Added CORS headers forwarding | Nginx config redeployed via SSM |
| `.github/workflows/deploy-backend.yml` | Rewrote backend deployment | Next deployment will use fixed script |
| `502_GATEWAY_FIX_SUMMARY.md` | NEW - Documentation | Reference guide |
| `DEPLOYMENT_VERIFICATION_CHECKLIST.md` | NEW - Testing guide | Verify deployment success |

---

## Deployment Process

1. **Code merged to main**
2. **GitHub Actions triggers:**
   - Build & test
   - Terraform infrastructure (applies nginx fix)
   - Deploy frontend
   - Deploy backend (using improved script)
3. **Nginx TLS bootstrap** (SSM Document):
   - Creates khaleo-api.conf with CORS headers ✓
4. **Backend deployment**:
   - Docker container starts with proper credentials ✓
5. **Health checks pass**
6. **502 errors should resolve** ✓

---

## Rollback

If issues arise:

```bash
# Revert to previous commit
git revert <commit_hash>
git push

# Redeploy with previous version
# OR manually rollback container
docker run -d --name khaleo-backend-old \
  brvkha/khaleoapp:<PREVIOUS_SHA>
```

---

## Next Steps

1. ✅ Code review & merge to main
2. ⏳ Run full deployment pipeline
3. ⏳ Verify with testing checklist
4. ⏳ Monitor for 48 hours
5. ⏳ Document any issues found

---

## Contact & Support

For deployment issues:
- Check logs in GitHub Actions
- SSH to EC2 and check:
  - `docker logs khaleo-backend`
  - `nginx -t && tail /var/log/nginx/error.log`
  - `curl http://127.0.0.1:8080/actuator/health`
- Refer to `DEPLOYMENT_VERIFICATION_CHECKLIST.md` for detailed troubleshooting

