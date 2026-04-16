# 502 Gateway Error - Complete Fix Implementation Guide

## Status: ✅ CODE FIXES COMPLETE

This document provides a complete guide to resolving the 502 Gateway error caused by a database schema mismatch.

---

## Problem Summary

**Error:** `Schema-validation: missing column [parent_id] in table [decks]`

**Root Cause:**
- Flyway migration `V8__nested_decks_and_ielts_vocab.sql` was not being executed in the correct order
- Due to alphanumeric sorting, single-digit version `V8` was sorted before date-based versions like `V20260319_008`
- Result: Migration never ran, so `parent_id` column was never added to the database
- When application started with `ddl-auto=validate`, it detected the mismatch and failed

---

## Solution Overview

The fix involves TWO coordinated changes:

### 1. **Code Fix: Migration Rename** ✅ DONE
- **File:** `backend/src/main/resources/db/migration/V8__nested_decks_and_ielts_vocab.sql`
- **Action:** Renamed to `V20260416_014__nested_decks_and_ielts_vocab.sql`
- **Why:** Ensures Flyway executes migrations in the correct sequence
- **Status:** Already implemented in git

### 2. **Code Fix: Production Configuration** ✅ DONE
- **File Created:** `backend/src/main/resources/application-production.yml`
- **Settings:** 
  - `spring.jpa.hibernate.ddl-auto: validate` (strict schema validation)
  - Import production secrets from `/opt/khaleo/flashcard-backend/runtime-secrets.env`
- **Why:** Ensures production environment validates schema against database
- **Status:** File created, ready for deployment

### 3. **Database Fix: Add Missing Column** ⏳ PENDING EXECUTION
- **Action:** Execute ALTER TABLE on RDS
- **Why:** Adds the missing `parent_id` column to the production database
- **Status:** Awaiting execution (see instructions below)

---

## Migration File Details

### Current Migration: V20260416_014__nested_decks_and_ielts_vocab.sql

```sql
ALTER TABLE decks ADD COLUMN parent_id CHAR(36) NULL;
ALTER TABLE decks ADD CONSTRAINT fk_deck_parent 
  FOREIGN KEY (parent_id) REFERENCES decks(id) ON DELETE CASCADE;

-- Default IELTS Parent Deck
INSERT INTO decks (id, author_id, name, description, is_public, tags, parent_id, created_at, updated_at)
VALUES ('550e8400-e29b-41d4-a716-446655440000', '00000000-0000-0000-0000-000000000002', 'IELTS', 'IELTS Vocabulary Collection', TRUE, 'ielts,vocabulary', NULL, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));

-- Default Sub-deck for Vocabulary
INSERT INTO decks (id, author_id, name, parent_id, description, is_public, tags, created_at, updated_at)
VALUES ('550e8400-e29b-41d4-a716-446655440001', '00000000-0000-0000-0000-000000000002', 'Vocabulary List 1', '550e8400-e29b-41d4-a716-446655440000', 'Extracted from Excel', TRUE, 'vocabulary,words', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));
```

### Migration Execution Order

Flyway will now execute in this order (after fix):
1. V1__init_schema.sql
2. V2__auth_security_schema.sql
3. V3__deck_card_media_schema.sql
4. V4__study_state_fields.sql
5. V5__admin_moderation_schema.sql
6. V6__seed_phase1_user.sql
7. V7__sample_accounts.sql
8. V20260319_008__public_clone_merge.sql
9. V20260323_009__fsrs_v4_state_fields.sql
10. V20260324_010__deck_ban_fields.sql
11. V20260324_011__study_algorithm_settings.sql
12. V20260326_012__rich_card_content.sql
13. V20260416_013__user_timezone_and_study_reset.sql
14. **V20260416_014__nested_decks_and_ielts_vocab.sql** ← YOUR FIX

---

## Implementation Steps

### Step 1: Update Database (One-time Manual Fix)

This adds the missing column to your production RDS database.

**Option A: Quick Manual Fix via RDS Console**

1. Go to AWS RDS Console
2. Select `khaleoapp-prod-db` instance
3. Use Query Editor or MySQL client to connect
4. Execute:

```sql
-- Add the missing column
ALTER TABLE decks ADD COLUMN parent_id CHAR(36) NULL;

-- Add the foreign key constraint
ALTER TABLE decks ADD CONSTRAINT fk_deck_parent 
  FOREIGN KEY (parent_id) REFERENCES decks(id) ON DELETE CASCADE;

-- Verify the change
DESC decks;
SELECT * FROM flyway_schema_history ORDER BY version DESC LIMIT 5;
```

**Option B: Via EC2 Instance (Using MySQL CLI)**

```bash
# SSH into EC2
aws ssm start-session --target <INSTANCE_ID> --region ap-southeast-1

# Get RDS credentials
sudo cat /opt/khaleo/flashcard-backend/runtime-secrets.env

# Connect to RDS
mysql -h khaleoapp-prod-db.cl8m6uq24vd0.ap-southeast-1.rds.amazonaws.com \
  -u <DB_USER> \
  -p<DB_PASSWORD> \
  khaleoapp

# Execute the ALTER commands above
```

**Option C: Via SSM Send Command (Fully Automated)**

```bash
aws ssm send-command \
  --instance-ids <EC2_INSTANCE_ID> \
  --document-name "AWS-RunShellScript" \
  --parameters 'commands=[
    "mysql -h khaleoapp-prod-db.cl8m6uq24vd0.ap-southeast-1.rds.amazonaws.com -u$(grep DB_USERNAME /opt/khaleo/flashcard-backend/runtime-secrets.env | cut -d= -f2) -p$(grep DB_PASSWORD /opt/khaleo/flashcard-backend/runtime-secrets.env | cut -d= -f2) khaleoapp <<EOF",
    "ALTER TABLE decks ADD COLUMN parent_id CHAR(36) NULL;",
    "ALTER TABLE decks ADD CONSTRAINT fk_deck_parent FOREIGN KEY (parent_id) REFERENCES decks(id) ON DELETE CASCADE;",
    "DESC decks;",
    "EOF"
  ]' \
  --region ap-southeast-1
```

### Step 2: Redeploy Backend Container

Once the database schema is fixed, redeploy the backend:

```bash
# Trigger GitHub Actions to rebuild and deploy
git push origin main

# OR manually restart the container if already built
aws ssm send-command \
  --instance-ids <EC2_INSTANCE_ID> \
  --document-name "AWS-RunShellScript" \
  --parameters 'commands=[
    "docker stop khaleo-backend || true",
    "docker rm khaleo-backend || true",
    "docker pull brvkha/khaleoapp:latest",
    "docker run -d --restart unless-stopped --name khaleo-backend \\",
    "  -p 8080:8080 \\",
    "  --env-file /opt/khaleo/flashcard-backend/runtime-secrets.env \\",
    "  -e SPRING_PROFILES_ACTIVE=production \\",
    "  -e SPRING_JPA_HIBERNATE_DDL_AUTO=validate \\",
    "  brvkha/khaleoapp:latest",
    "sleep 15",
    "docker logs --tail 100 khaleo-backend",
    "curl -v http://127.0.0.1:8080/actuator/health"
  ]' \
  --region ap-southeast-1
```

### Step 3: Verify the Fix

Check that the application is running correctly:

```bash
# 1. Check container is running
docker ps -a --filter name=khaleo-backend

# 2. Check application logs
docker logs -f --tail 100 khaleo-backend

# 3. Check health endpoint
curl http://127.0.0.1:8080/actuator/health

# 4. Check database schema
mysql -h khaleoapp-prod-db.cl8m6uq24vd0.ap-southeast-1.rds.amazonaws.com \
  -u <user> -p<password> khaleoapp -e "DESC decks;" | grep parent_id

# 5. Check Flyway migration status
mysql -h khaleoapp-prod-db.cl8m6uq24vd0.ap-southeast-1.rds.amazonaws.com \
  -u <user> -p<password> khaleoapp -e "SELECT * FROM flyway_schema_history WHERE version >= 'V20260416_013';"
```

---

## Verification Checklist

After implementing the fix, verify the following:

- [ ] RDS database has `parent_id` column in `decks` table
- [ ] RDS database has `fk_deck_parent` foreign key constraint
- [ ] Container started successfully without schema validation errors
- [ ] Application logs show: "Application started successfully"
- [ ] Health endpoint (`/actuator/health`) returns 200 OK
- [ ] Nginx returns 200 OK (no 502 Gateway errors)
- [ ] API endpoints are responding correctly

---

## Files Modified

| File | Type | Status | Reason |
|------|------|--------|--------|
| `backend/src/main/resources/db/migration/V8__nested_decks_and_ielts_vocab.sql` | Migration | ✅ Renamed | Corrected migration sequence |
| `backend/src/main/resources/application-production.yml` | Config | ✅ Created | Production-specific schema validation |
| RDS Database | External | ⏳ Pending | Add `parent_id` column |

---

## Why This Fix Works

### Before Fix

```
Git Flow:
  ├─ V1-V7 (numeric) 
  ├─ V8 (numeric) ← WRONG: Gets sorted after V20260326_012 due to alphanumeric sorting
  └─ V20260319_008+ (date-based)

Flyway Execution:
  V1 → V2 → V3 → V4 → V5 → V6 → V7 → V20260319_008 → ... → V20260416_013 → (V8 NEVER RUNS!)

Result:
  ✗ parent_id column never added
  ✗ Schema validation fails when application starts
  ✗ 502 Gateway Error
```

### After Fix

```
Git Flow:
  ├─ V1-V7 (numeric) 
  └─ V20260319_008+ (date-based) ← Consistent
      └─ V20260416_014 (date-based) ← YOUR FIX

Flyway Execution:
  V1 → V2 → V3 → ... → V20260416_013 → V20260416_014 ✓

Result:
  ✓ parent_id column added by V20260416_014
  ✓ Schema validation passes
  ✓ Application starts successfully
  ✓ 502 Gateway Error resolved
```

---

## Rollback Plan

If something goes wrong, you have multiple options:

### Option 1: Revert Schema Change (Safest)
```sql
-- Remove the column and constraint
ALTER TABLE decks DROP CONSTRAINT fk_deck_parent;
ALTER TABLE decks DROP COLUMN parent_id;

-- Mark migration as failed in Flyway
DELETE FROM flyway_schema_history 
WHERE version = 'V20260416_014';
```

### Option 2: Restart with Previous Build
```bash
# Go back to previous working image
docker stop khaleo-backend
docker rm khaleo-backend
docker pull brvkha/khaleoapp:previous-tag
docker run -d --restart unless-stopped --name khaleo-backend \
  -p 8080:8080 \
  --env-file /opt/khaleo/flashcard-backend/runtime-secrets.env \
  -e SPRING_PROFILES_ACTIVE=production \
  brvkha/khaleoapp:previous-tag
```

### Option 3: Manual Database Restore
- Use RDS automated snapshot restore if available
- Contact AWS Support for point-in-time recovery

---

## Prevention for Future

1. **Always use date-based migration naming:**
   ```
   ✓ CORRECT: V20260416_015__description.sql
   ✗ WRONG: V15__description.sql
   ```

2. **Verify migration order locally:**
   ```bash
   mvn flyway:info -Dflyway.configFiles=backend/pom.xml
   ```

3. **Test schema validation in CI/CD:**
   ```bash
   SPRING_JPA_HIBERNATE_DDL_AUTO=validate mvn test
   ```

4. **Code review checklist:**
   - ✓ Migration naming follows date-based format
   - ✓ Migration sequenced after latest existing migration
   - ✓ Entity model matches migration schema
   - ✓ Tested locally with validation mode

---

## Support

If you need help:

1. **Check logs:** `docker logs -f khaleo-backend`
2. **Verify schema:** `DESC decks; SELECT * FROM flyway_schema_history;`
3. **Test locally:** Build and test schema validation locally before deploying
4. **See SOLUTION_SUMMARY.md** for additional context

---

**Last Updated:** April 16, 2026
**Fix Status:** ✅ Code fixes complete, awaiting database execution

