# 502 Bad Gateway - CORS Error Fix Summary

## Vấn đề được phát hiện
1. **502 Bad Gateway**: Backend container không chạy hoặc không respond
2. **CORS Policy Error**: Nginx proxy không forward CORS headers từ backend
3. **Deployment Script Issues**: Deploy script chưa handle credentials đúng cách

## Các Fix đã áp dụng

### 1. Fix Nginx CORS Headers (infra/terraform/app/main.tf - line 340)
**Vấn đề**: Nginx proxy không forward CORS response headers từ backend

**Fix**: Thêm các proxy_pass_header directives:
```nginx
proxy_pass_header Access-Control-Allow-Origin;
proxy_pass_header Access-Control-Allow-Methods;
proxy_pass_header Access-Control-Allow-Headers;
proxy_pass_header Access-Control-Allow-Credentials;
proxy_pass_header Access-Control-Max-Age;
proxy_pass_header Access-Control-Expose-Headers;
proxy_set_header X-Forwarded-Host $server_name;
proxy_set_header X-Forwarded-Port $server_port;
```

**Tác động**: Cấu hình Nginx sẽ được deploy lại qua SSM document khi chạy deploy workflow.

### 2. Fix Backend Deployment Script (.github/workflows/deploy-backend.yml)
**Vấn đề**: 
- Docker credentials không được pass đúng cách
- RDS credentials không được escape properly
- No proper error handling/logging

**Fixes**:
- Tách credentials handling thành một section rõ ràng
- Sử dụng file-based parameters cho SSM để avoid shell escaping issues
- Thêm extensive logging (docker ps, logs, health check)
- Wait timeout tăng từ 30s lên 120s để container có thời gian start

**Chi tiết changes**:
```yaml
# Trước: Direct inline docker run command
docker run -d ... -e DB_PASSWORD='${RDS_DB_PASSWORD}' ...

# Sau: File-based parameter substitution
cat > /tmp/deploy_params.json <<'EOF'
{ "commands": [...] }
EOF
sed -i "s|PLACEHOLDER|VALUE|g" /tmp/deploy_params.json
aws ssm send-command --parameters file:///tmp/deploy_params.json
```

### 3. Spring Boot Configuration Check
**File**: backend/src/main/resources/application.yml
- **Profile: prod** - sử dụng environment variables cho DB credentials
- **CORS Config**: SecurityConfig.java (line 30) đã có whitelist origins
  - https://api.khaleoshop.click
  - https://khaleoshop.click
  - https://www.khaleoshop.click

## Deployment Flow

### Khi chạy workflow:

1. **Build phase**:
   - Build JAR
   - Build & push Docker image

2. **Infrastructure**:
   - Terraform provision EC2, RDS, Nginx config

3. **Bootstrap Nginx/TLS** (SSM Document):
   - Install certbot, nginx
   - Generate TLS certificates
   - Configure proxy to backend:8080
   - **← FIXED**: Now includes CORS headers forwarding

4. **Deploy Backend** (NEW):
   - Resolve RDS endpoint
   - Create parameter file với credentials
   - Send SSM command để deploy container
   - Docker pull & run container
   - Health check via localhost:8080/actuator/health

## Testing Checklist

```bash
# 1. Check container is running
docker ps | grep khaleo-backend

# 2. Check backend logs
docker logs -f khaleo-backend

# 3. Local health check
curl http://127.0.0.1:8080/actuator/health

# 4. CORS preflight check
curl -i -X OPTIONS \
  -H "Origin: https://khaleoshop.click" \
  -H "Access-Control-Request-Method: POST" \
  https://api.khaleoshop.click/api/v1/auth/login

# 5. Check nginx logs
tail -50 /var/log/nginx/access.log
tail -50 /var/log/nginx/error.log

# 6. Check nginx config
nginx -t
cat /etc/nginx/conf.d/khaleo-api.conf
```

## Các biến cần thiết trong GitHub Secrets

```
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
DOCKERHUB_USERNAME
DOCKERHUB_TOKEN
TLS_EMAIL
RDS_DB_NAME
RDS_DB_USER
RDS_DB_PASSWORD
JWT_SECRET (hoặc JWT_SECRET_PROD)
EC2_INSTANCE_ID (optional, sẽ auto-discover từ tags)
```

## Next Steps

1. **Chạy deployment pipeline**:
   - GitHub Actions > Main Deployment Pipeline > Run workflow

2. **Monitor deployment**:
   - Watch Docker container startup
   - Check backend logs for Spring Boot initialization
   - Verify Nginx CORS headers

3. **Test CORS**:
   ```bash
   # From browser console
   fetch('https://api.khaleoshop.click/api/v1/auth/login', {
     method: 'POST',
     credentials: 'include',
     headers: { 'Content-Type': 'application/json' }
   })
   ```

## Troubleshooting

### 502 Bad Gateway still appears
1. SSH vào EC2 (qua SSM Session Manager)
2. `docker logs khaleo-backend` - check application startup errors
3. `systemctl status nginx` - verify nginx is running
4. `curl -v http://127.0.0.1:8080/actuator/health` - test backend directly

### CORS still failing
1. Check nginx config has proxy_pass_header directives
2. Verify backend CORS config in SecurityConfig.java
3. Check Origin header matches allowed origins list
4. Test OPTIONS request separately: `curl -v -X OPTIONS ...`

### Health check timeout
1. Check RDS connection: `docker logs khaleo-backend | grep -i database`
2. Verify Flyway migrations: `docker logs khaleo-backend | grep -i flyway`
3. Check JWT_SECRET is set: `docker inspect khaleo-backend | grep JWT_SECRET`

