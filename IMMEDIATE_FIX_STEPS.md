# Production Fix Instructions - 502 Bad Gateway Error

## Quick Summary
The backend fails because the database is missing the `parent_id` column on the `decks` table. This happened because a migration file was named `V8` which Flyway doesn't execute in the correct order.

## IMMEDIATE FIX (30 seconds)

### Option A: Via SSM Command (Recommended - No EC2 SSH needed)

Run this AWS CLI command from your local machine:

```bash
aws ssm send-command \
  --instance-ids "i-xxxxx" \
  --document-name "AWS-RunShellScript" \
  --parameters 'commands=[
    "mysql -h khaleoapp-prod-db.cl8m6uq24vd0.ap-southeast-1.rds.amazonaws.com -u $RDS_USER -p$RDS_PASS khaleoapp -e \"ALTER TABLE decks ADD COLUMN parent_id CHAR(36) NULL;\"",
    "mysql -h khaleoapp-prod-db.cl8m6uq24vd0.ap-southeast-1.rds.amazonaws.com -u $RDS_USER -p$RDS_PASS khaleoapp -e \"ALTER TABLE decks ADD CONSTRAINT fk_deck_parent FOREIGN KEY (parent_id) REFERENCES decks(id) ON DELETE CASCADE;\""
  ]' \
  --region ap-southeast-1
```

### Option B: Direct SSH to EC2 (If you have SSH access)

1. SSH into EC2 instance
2. Get RDS credentials from environment:
```bash
docker inspect khaleo-backend | grep "DB_"
```

3. Connect to MySQL and add the column:
```bash
mysql -h khaleoapp-prod-db.cl8m6uq24vd0.ap-southeast-1.rds.amazonaws.com \
  -u khaleoapp_user \
  -p
# Then type password when prompted

# Inside MySQL:
USE khaleoapp;
ALTER TABLE decks ADD COLUMN parent_id CHAR(36) NULL;
ALTER TABLE decks ADD CONSTRAINT fk_deck_parent FOREIGN KEY (parent_id) REFERENCES decks(id) ON DELETE CASCADE;
EXIT;
```

4. Restart the container:
```bash
docker restart khaleo-backend
docker logs -f khaleo-backend
```

### Option C: Full Redeploy (Ensures code is also updated)

Push code and trigger deployment:
```bash
git push origin main
# This will trigger GitHub Actions deploy-backend.yml workflow
```

The new code includes the properly-named migration `V20260416_014__nested_decks_and_ielts_vocab.sql` which will execute correctly on redeploy.

## Verification

Once container restarts, verify:
```bash
# Check container is running
docker ps | grep khaleo-backend

# Check logs for success
docker logs --tail 50 khaleo-backend

# Test API health
curl http://localhost:8080/actuator/health

# From outside EC2 (if API domain is set up):
curl https://api.khaleoshop.click/actuator/health
```

## Long-term Fix (Already Applied)

1. Migration file renamed from `V8__nested_decks_and_ielts_vocab.sql` to `V20260416_014__nested_decks_and_ielts_vocab.sql`
2. This ensures Flyway executes it in proper order after V20260326_012
3. Future deployments will apply this migration correctly

## Why This Happened

- New migration was created with old naming scheme (V8)
- Flyway uses alphanumeric sorting, so V8 comes AFTER V20260326_012
- The migration never executed in production
- When Hibernate validated the schema, it found the entity has `parent_id` but the table doesn't

## No Rollback Needed

Adding a nullable column to an empty/existing table is safe and doesn't require rollback logic.

