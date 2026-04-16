# Deployment Verification Checklist

## Pre-Deployment

- [ ] Verify AWS credentials are configured locally
  ```bash
  aws sts get-caller-identity
  ```

- [ ] Verify GitHub Secrets are set:
  - [ ] AWS_ACCESS_KEY_ID
  - [ ] AWS_SECRET_ACCESS_KEY
  - [ ] DOCKERHUB_USERNAME
  - [ ] DOCKERHUB_TOKEN
  - [ ] RDS_DB_NAME, RDS_DB_USER, RDS_DB_PASSWORD
  - [ ] JWT_SECRET or JWT_SECRET_PROD
  - [ ] TLS_EMAIL

- [ ] Verify EC2 Instance:
  ```bash
  aws ec2 describe-instances --instance-ids i-0b5593444f9789e47 \
    --query 'Reservations[0].Instances[0].[State.Name,PublicIpAddress]'
  ```

- [ ] Verify SSM Agent:
  ```bash
  aws ssm describe-instance-information \
    --filters "Key=InstanceIds,Values=i-0b5593444f9789e47" \
    --query 'InstanceInformationList[0].PingStatus'
  ```
  Expected: `Online`

## During Deployment

### Workflow Stages

- [ ] **Stage 1: Build & Test**
  - Frontend tests (npm ci && npm test)
  - Backend tests (mvn test)
  - Docker image build & push

- [ ] **Stage 2: Terraform Infrastructure**
  - VPC, Subnets, Security Groups
  - EC2, RDS, S3, CloudFront
  - Nginx/TLS bootstrap via SSM

- [ ] **Stage 3: Frontend Deployment**
  - S3 bucket upload
  - CloudFront invalidation

- [ ] **Stage 4: Backend Deployment** ← Critical for 502 fix
  - Docker image pull
  - Container stop/remove old
  - Container start new with credentials
  - Health check

### Monitor Deployment Progress

```bash
# Watch GitHub Actions logs in real-time
# OR via AWS CLI:

# Get EC2 instance info
aws ec2 describe-instances --instance-ids i-0b5593444f9789e47 \
  --region ap-southeast-1 \
  --query 'Reservations[0].Instances[0]'

# Check SSM command status during deployment
aws ssm list-command-invocations \
  --command-id <COMMAND_ID_FROM_LOGS> \
  --region ap-southeast-1 \
  --details
```

## Post-Deployment Verification

### 1. EC2 Instance & Services

- [ ] SSH to EC2 via SSM Session Manager:
  ```bash
  aws ssm start-session --target i-0b5593444f9789e47 \
    --region ap-southeast-1
  ```

- [ ] Check Docker container:
  ```bash
  docker ps | grep khaleo-backend
  # Expected: Running with "khaleo-backend" name
  ```

- [ ] Check backend logs:
  ```bash
  docker logs -f khaleo-backend --tail 100
  # Look for:
  # - "Started FlashcardApplication"
  # - No "Connection refused" or database errors
  ```

- [ ] Health check localhost:
  ```bash
  curl http://127.0.0.1:8080/actuator/health
  # Expected: {"status":"UP"}
  ```

### 2. Nginx Configuration

- [ ] Check nginx is running:
  ```bash
  systemctl status nginx
  systemctl is-active nginx
  ```

- [ ] Test nginx config syntax:
  ```bash
  nginx -t
  # Expected: "successful"
  ```

- [ ] Verify CORS headers in config:
  ```bash
  grep -A 30 "location /" /etc/nginx/conf.d/khaleo-api.conf
  # Should see:
  # - proxy_pass_header Access-Control-Allow-Origin
  # - proxy_pass_header Access-Control-Allow-Methods
  # - proxy_pass_header Access-Control-Allow-Headers
  # - proxy_pass_header Access-Control-Allow-Credentials
  ```

- [ ] Check nginx logs:
  ```bash
  tail -50 /var/log/nginx/access.log
  tail -20 /var/log/nginx/error.log
  ```

### 3. CORS Testing

Test from local machine or browser console:

```bash
# Test preflight request
curl -i -X OPTIONS \
  -H "Origin: https://khaleoshop.click" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type" \
  https://api.khaleoshop.click/api/v1/auth/login

# Expected response headers:
# Access-Control-Allow-Origin: https://khaleoshop.click
# Access-Control-Allow-Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
# Access-Control-Allow-Headers: *
# Access-Control-Allow-Credentials: true
```

```javascript
// Browser console test
fetch('https://api.khaleoshop.click/api/v1/auth/login', {
  method: 'POST',
  credentials: 'include',
  headers: {
    'Content-Type': 'application/json',
    'Origin': 'https://khaleoshop.click'
  },
  body: JSON.stringify({
    email: 'test@example.com',
    password: 'password'
  })
})
.then(r => {
  console.log('Status:', r.status);
  console.log('Headers:', r.headers);
  return r.json();
})
.catch(e => console.error('Error:', e))
```

### 4. Full Integration Test

```bash
# 1. Test frontend can reach API
curl -v https://api.khaleoshop.click/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -H "Origin: https://khaleoshop.click" \
  -d '{"email":"test@example.com","password":"test"}'

# 2. Check frontend is accessible
curl https://khaleoshop.click | head -20

# 3. Monitor backend for requests
docker logs -f khaleo-backend --tail 50
```

## Troubleshooting

### 502 Bad Gateway Still Appears

1. **Backend container not running**:
   ```bash
   docker ps -a | grep khaleo-backend
   docker logs khaleo-backend --tail 50
   ```
   → Look for: Spring Boot startup errors, database connection failures

2. **Backend not listening on 8080**:
   ```bash
   netstat -tlnp 2>/dev/null | grep 8080
   # OR
   ss -tlnp | grep 8080
   ```

3. **Nginx not forwarding to backend**:
   ```bash
   nginx -T | grep -A 50 "khaleo-api"
   curl -v http://127.0.0.1:8080/actuator/health
   ```

### CORS Preflight Still Failing

1. **Nginx not forwarding CORS headers**:
   ```bash
   # Check if proxy_pass_header directives are in config
   grep "proxy_pass_header Access-Control" /etc/nginx/conf.d/khaleo-api.conf
   ```

2. **Backend CORS not configured for origin**:
   ```bash
   # Check backend logs for CORS validation
   docker logs khaleo-backend | grep -i cors
   ```

3. **Certificate/HTTPS issues**:
   ```bash
   # Test HTTP vs HTTPS
   curl -k https://api.khaleoshop.click/api/v1/auth/login
   # Check certificate
   echo | openssl s_client -connect api.khaleoshop.click:443 -showcerts
   ```

### Health Check Timeout

1. **Spring Boot not fully started**:
   ```bash
   docker logs khaleo-backend | grep -E "(Started|ERROR|Exception)"
   ```

2. **Database migration issues**:
   ```bash
   docker logs khaleo-backend | grep -i flyway
   ```

3. **Missing environment variables**:
   ```bash
   docker inspect khaleo-backend | grep -A 20 Env
   ```

## Rollback Plan

If deployment fails:

1. **Keep previous docker image available**:
   ```bash
   docker images | grep khaleoapp
   ```

2. **Rollback to previous version**:
   ```bash
   docker run -d --name khaleo-backend-old \
     brvkha/khaleoapp:<PREVIOUS_SHA>
   ```

3. **Or rerun deployment workflow** with last known-good commit SHA

## Documentation Updates

After successful deployment:

- [ ] Update deployment runbook with any issues found
- [ ] Document any environment-specific configurations
- [ ] Verify all monitoring/alarms are working
- [ ] Confirm database backups are enabled
- [ ] Test failover/disaster recovery procedures

