# EC2 Production Fix Checklist

**Status:** IN PROGRESS
**Date:** 2026-04-16
**Ticket:** 502 Bad Gateway - Schema Validation Error

## Phase 1: Database Fix ☐

### 1.1 Connect to RDS Database ☐
```bash
# Option 1: From EC2 instance
mysql -h khaleoapp-prod-db.cl8m6uq24vd0.ap-southeast-1.rds.amazonaws.com \
  -u <username> \
  -p<password> \
  khaleoapp
```

### 1.2 Add Missing parent_id Column ☐
```sql
-- Run these commands in MySQL console:
USE khaleoapp;

-- Add the column
ALTER TABLE decks ADD COLUMN parent_id CHAR(36) NULL;

-- Add foreign key constraint
ALTER TABLE decks ADD CONSTRAINT fk_deck_parent 
  FOREIGN KEY (parent_id) REFERENCES decks(id) ON DELETE CASCADE;

-- Verify the column was added
DESCRIBE decks;
-- Should show: parent_id | char(36) | YES | MUL | NULL
```

### 1.3 Verify Existing Decks (Optional) ☐
```sql
-- Check current data
SELECT id, name, author_id, parent_id FROM decks LIMIT 5;

-- Verify no data issues
SELECT COUNT(*) as total_decks FROM decks;
```

**Expected Result:** Column added successfully, no errors

---

## Phase 2: Container Fix ☐

### 2.1 Stop Old Container ☐
```bash
docker stop khaleo-backend
docker rm khaleo-backend
```

### 2.2 Verify Latest Image ☐
```bash
# Pull the latest image
docker pull brvkha/khaleoapp:latest

# Check available tags
docker images | grep khaleoapp
```

### 2.3 Get Runtime Secrets ☐
```bash
# Verify environment file exists
ls -la /opt/khaleo/flashcard-backend/runtime-secrets.env

# Verify permissions
stat /opt/khaleo/flashcard-backend/runtime-secrets.env
# Should show: 600 (read/write for owner only)
```

### 2.4 Start Container ☐
```bash
docker run -d \
  --restart unless-stopped \
  --name khaleo-backend \
  -p 8080:8080 \
  --env-file /opt/khaleo/flashcard-backend/runtime-secrets.env \
  -e SPRING_PROFILES_ACTIVE=production \
  -e SPRING_JPA_HIBERNATE_DDL_AUTO=validate \
  brvkha/khaleoapp:latest
```

**Wait 20-30 seconds for container to start...**

---

## Phase 3: Verification ☐

### 3.1 Container Status ☐
```bash
# Check container is running
docker ps | grep khaleo-backend
# Expected: Container should be UP (not Exited)

# If not running, check logs:
docker logs khaleo-backend
```

### 3.2 Application Logs ☐
```bash
# Watch last 50 lines
docker logs -f --tail 50 khaleo-backend

# Look for SUCCESS indicators:
# ✓ "schema `khaleoapp` is up to date"
# ✓ "Tomcat started on port(s): 8080"
# ✓ "Application started successfully"

# NOT should NOT see:
# ✗ "Schema-validation: missing column [parent_id]"
# ✗ "Application run failed"
# ✗ "Unable to start embedded Tomcat"
```

### 3.3 Health Check (Internal) ☐
```bash
# From inside EC2:
curl -i http://127.0.0.1:8080/actuator/health

# Expected Response:
# HTTP/1.1 200 OK
# {"status":"UP"}
```

### 3.4 Health Check (External) ☐
```bash
# From your local machine:
curl -i https://api.khaleoshop.click/actuator/health

# Expected Response:
# HTTP/1.1 200 OK
# {"status":"UP"}
```

### 3.5 Nginx Status ☐
```bash
# Check Nginx is running
sudo systemctl status nginx

# Check Nginx logs
sudo tail -20 /var/log/nginx/access.log
sudo tail -20 /var/log/nginx/error.log

# Look for 502 errors - should be gone
```

---

## Phase 4: Full Test ☐

### 4.1 Test API Endpoint ☐
```bash
# Example: List decks
curl -X GET https://api.khaleoshop.click/api/v1/decks \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Expected Response: 200 OK with deck list
```

### 4.2 Smoke Test Checklist ☐
- [ ] No 502 Bad Gateway errors
- [ ] All health checks return 200 OK
- [ ] Container logs show application started
- [ ] Database column `parent_id` exists
- [ ] Nginx reverse proxy is forwarding correctly

---

## Rollback Plan (If Needed)

If something goes wrong:

```bash
# Stop the new container
docker stop khaleo-backend

# Restore from last known good state
# (You would need to have the previous image/backup)
# OR redeploy with explicit image tag
docker run -d \
  --restart unless-stopped \
  --name khaleo-backend \
  -p 8080:8080 \
  --env-file /opt/khaleo/flashcard-backend/runtime-secrets.env \
  -e SPRING_PROFILES_ACTIVE=production \
  brvkha/khaleoapp:PREVIOUS_COMMIT_SHA
```

---

## Completion Summary

| Task | Status | Time | Notes |
|------|--------|------|-------|
| Database fix | ☐ | - | Add parent_id column |
| Container restart | ☐ | - | Pull latest, start new |
| Health check | ☐ | - | Verify 200 OK |
| API test | ☐ | - | Test actual endpoint |
| **COMPLETE** | ☐ | - | All green |

---

## Troubleshooting Reference

| Issue | Solution |
|-------|----------|
| Container exits immediately | Check logs: `docker logs khaleo-backend` |
| "missing column [parent_id]" | Ensure database ALTER TABLE commands were executed |
| 502 Bad Gateway still appears | Check Nginx: `sudo systemctl status nginx` |
| Health check returns 503 | App still starting, wait 30 seconds |
| Cannot connect to RDS | Check security groups, RDS endpoint, credentials |

---

**Status Log:**
- Started: [TIMESTAMP]
- Database fixed: [TIMESTAMP]  
- Container restarted: [TIMESTAMP]
- Verified: [TIMESTAMP]
- Completed: [TIMESTAMP]

