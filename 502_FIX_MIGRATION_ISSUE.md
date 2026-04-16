# 502 Gateway Error - Root Cause & Fix

## Problem
The backend container fails to start with error:
```
Schema-validation: missing column [parent_id] in table [decks]
```

## Root Cause
1. The `Deck` entity has a `parent_id` column defined (lines 44-46 in `Deck.java`)
2. A migration file `V8__nested_decks_and_ielts_vocab.sql` was created to add this column
3. **However**, due to Flyway version number sorting, `V8` comes AFTER `V20260326_012` (and all V20260319+ migrations) in alphanumeric order
4. Result: The migration never executed on the production database
5. When Hibernate validates the schema against the entity, it fails because the column doesn't exist

## Solution Applied

### Step 1: Fix Migration Naming (DONE ✓)
Renamed the migration file from:
- `V8__nested_decks_and_ielts_vocab.sql` 
to:
- `V20260416_014__nested_decks_and_ielts_vocab.sql`

This ensures Flyway executes it in the correct order after `V20260416_013__user_timezone_and_study_reset.sql`.

Commit: `ad60091` - "fix: rename V8 migration to V20260416_014 for proper Flyway execution order"

### Step 2: Immediate Fix for Production Database

Connect to the RDS database and run these SQL commands to add the missing column:

```sql
ALTER TABLE decks ADD COLUMN parent_id CHAR(36) NULL;
ALTER TABLE decks ADD CONSTRAINT fk_deck_parent FOREIGN KEY (parent_id) REFERENCES decks(id) ON DELETE CASCADE;
```

**From EC2 instance**, you can execute this via MySQL client:
```bash
mysql -h khaleoapp-prod-db.cl8m6uq24vd0.ap-southeast-1.rds.amazonaws.com \
  -u <db_user> -p<db_password> \
  khaleoapp \
  -e "ALTER TABLE decks ADD COLUMN parent_id CHAR(36) NULL;"

mysql -h khaleoapp-prod-db.cl8m6uq24vd0.ap-southeast-1.rds.amazonaws.com \
  -u <db_user> -p<db_password> \
  khaleoapp \
  -e "ALTER TABLE decks ADD CONSTRAINT fk_deck_parent FOREIGN KEY (parent_id) REFERENCES decks(id) ON DELETE CASCADE;"
```

### Step 3: Restart Container

After adding the column to the database:

```bash
docker stop khaleo-backend
docker rm khaleo-backend
docker pull brvkha/khaleoapp:latest
docker run -d --restart unless-stopped --name khaleo-backend \
  -p 8080:8080 \
  --env-file /opt/khaleo/flashcard-backend/runtime-secrets.env \
  -e SPRING_PROFILES_ACTIVE=production \
  -e SPRING_JPA_HIBERNATE_DDL_AUTO=validate \
  brvkha/khaleoapp:latest
```

Or redeploy via the GitHub Actions workflow with the latest code that has the proper migration naming.

## Prevention
Always use consistent version numbering schemes for Flyway migrations. The codebase now uses date-based naming (V20260319_008+) consistently.

