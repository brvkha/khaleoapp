# 502 Gateway Error - Quick Execution Checklist

**Estimated Time:** 10-15 minutes  
**Risk Level:** LOW (schema addition only)  
**Rollback:** Available (see SOLUTION_SUMMARY.md)

---

## Pre-Execution Checklist

- [ ] Read 502_GATEWAY_FIX_COMPLETE.md for full context
- [ ] Verified RDS credentials are available
- [ ] EC2 instance is running and accessible via SSM
- [ ] Have MySQL client installed OR will use RDS console
- [ ] Backup or snapshot of database is available

---

## Step 1: Add Missing Column to RDS (2-3 minutes)

**Option A: Using MySQL CLI (Recommended)**

```bash
# Connect to RDS
mysql -h khaleoapp-prod-db.cl8m6uq24vd0.ap-southeast-1.rds.amazonaws.com \
  -u <REPLACE_WITH_DB_USER> \
  -p<REPLACE_WITH_DB_PASSWORD> \
  khaleoapp

# Copy-paste these commands into MySQL:
ALTER TABLE decks ADD COLUMN parent_id CHAR(36) NULL;
ALTER TABLE decks ADD CONSTRAINT fk_deck_parent FOREIGN KEY (parent_id) REFERENCES decks(id) ON DELETE CASCADE;
DESC decks;
SELECT VERSION FROM flyway_schema_history ORDER BY version DESC LIMIT 5;
EXIT;
```

**Option B: Using AWS RDS Query Editor**

1. Go to AWS Console → RDS → Databases → khaleoapp-prod-db
2. Click "Query editor" tab
3. Execute each SQL statement:

```sql
ALTER TABLE decks ADD COLUMN parent_id CHAR(36) NULL;
```

Then:

```sql
ALTER TABLE decks ADD CONSTRAINT fk_deck_parent 
FOREIGN KEY (parent_id) REFERENCES decks(id) ON DELETE CASCADE;
```

Then verify:

```sql
DESC decks;
```

- [ ] Column `parent_id` successfully added to `decks` table
- [ ] Foreign key constraint `fk_deck_parent` created
- [ ] No errors during execution

---

## Step 2: Restart Backend Container (3-5 minutes)

**Get RDS Credentials from EC2**

```bash
# SSH into EC2 via SSM
aws ssm start-session --target i-0fa666265f036c141 --region ap-southeast-1

# Inside EC2, view credentials:
sudo cat /opt/khaleo/flashcard-backend/runtime-secrets.env

# Exit SSM session
exit
```

**Stop and Remove Old Container**

```bash
# Via SSM
aws ssm send-command \
  --instance-ids i-0fa666265f036c141 \
  --document-name "AWS-RunShellScript" \
  --parameters 'commands=["docker stop khaleo-backend || true","docker rm khaleo-backend || true"]' \
  --region ap-southeast-1

# Wait for command to complete (usually 30 seconds)
```

**Start New Container**

```bash
aws ssm send-command \
  --instance-ids i-0fa666265f036c141 \
  --document-name "AWS-RunShellScript" \
  --parameters 'commands=[
    "docker login -u DOCKERHUB_USER -p DOCKERHUB_TOKEN",
    "docker pull brvkha/khaleoapp:latest",
    "docker run -d --restart unless-stopped --name khaleo-backend -p 8080:8080 --env-file /opt/khaleo/flashcard-backend/runtime-secrets.env -e SPRING_PROFILES_ACTIVE=production -e SPRING_JPA_HIBERNATE_DDL_AUTO=validate brvkha/khaleoapp:latest",
    "sleep 15",
    "echo Container Status:",
    "docker ps -a --filter name=khaleo-backend",
    "echo Application Logs:",
    "docker logs --tail 50 khaleo-backend"
  ]' \
  --region ap-southeast-1
```

- [ ] Container stopped and removed
- [ ] New container started successfully
- [ ] Container is in "healthy" or "Up" state

---

## Step 3: Verify Fix (2 minutes)

**Check Container Health**

```bash
aws ssm send-command \
  --instance-ids i-0fa666265f036c141 \
  --document-name "AWS-RunShellScript" \
  --parameters 'commands=["docker exec khaleo-backend curl -s http://localhost:8080/actuator/health"]' \
  --region ap-southeast-1
```

Expected response:
```json
{"status":"UP"}
```

**Check Application Logs**

```bash
aws ssm send-command \
  --instance-ids i-0fa666265f036c141 \
  --document-name "AWS-RunShellScript" \
  --parameters 'commands=["docker logs --tail 30 khaleo-backend | grep -E '\'(schema|validation|Application|error)\'"]' \
  --region ap-southeast-1
```

Expected: No schema validation errors, "Application started successfully"

**Check Nginx Response**

```bash
curl https://api.khaleoshop.click/actuator/health
# Should return 200 OK, not 502
```

- [ ] Container health check: ✓ UP
- [ ] Application logs: ✓ No schema validation errors
- [ ] Nginx: ✓ Returns 200 OK (not 502)
- [ ] API endpoint: ✓ Responding correctly

---

## Step 4: Final Verification (1 minute)

**Connect to RDS and verify migration was applied**

```bash
mysql -h khaleoapp-prod-db.cl8m6uq24vd0.ap-southeast-1.rds.amazonaws.com \
  -u <DB_USER> \
  -p<DB_PASSWORD> \
  khaleoapp -e "SELECT * FROM flyway_schema_history WHERE version LIKE 'V202604%' ORDER BY version;"
```

Expected output includes:
```
| version         | description                                    | success |
| V20260416_013   | user_timezone_and_study_reset                  | 1       |
| V20260416_014   | nested_decks_and_ielts_vocab                   | 1       |
```

- [ ] V20260416_014 migration is marked as successful (success = 1)

---

## Troubleshooting

### Issue: Column already exists
```
ERROR 1060: Duplicate column name 'parent_id'
```
**Solution:** Column is already added. Skip to Step 2.

### Issue: Foreign key constraint already exists
```
ERROR 1822: Failed to add the foreign key constraint
```
**Solution:** Constraint is already created. Continue to Step 2.

### Issue: Container fails to start
**Log:** Check `docker logs khaleo-backend` for errors
**Solution:** 
1. Verify RDS is accessible from EC2
2. Verify `runtime-secrets.env` file exists
3. Check database credentials in runtime-secrets.env

### Issue: 502 Gateway Error still appears
**Steps:**
1. Verify database schema change was applied: `DESC decks;`
2. Restart container again
3. Wait 30 seconds for startup to complete
4. Check logs for any remaining validation errors

---

## Success Criteria

✅ You have successfully fixed the 502 error when ALL of the following are true:

1. ✓ RDS table `decks` has column `parent_id`
2. ✓ Foreign key constraint `fk_deck_parent` exists
3. ✓ Container `khaleo-backend` is running
4. ✓ `docker logs khaleo-backend` shows no schema validation errors
5. ✓ Health endpoint returns 200 OK
6. ✓ Nginx returns 200 OK (not 502)
7. ✓ Flyway migration V20260416_014 is marked as successful

---

## Post-Execution

### Deploy Code Changes (Optional but Recommended)

The migration file has been renamed in git. Deploy this change so future deployments use the corrected migration:

```bash
cd /path/to/khaleoapp
git add backend/src/main/resources/db/migration/V20260416_014__nested_decks_and_ielts_vocab.sql
git add backend/src/main/resources/application-production.yml
git commit -m "fix: correct Flyway migration sequencing for nested decks feature"
git push origin main
```

This ensures:
- New deployments use the corrected migration
- Other developers see the fix in code
- CI/CD pipeline can validate the fix

### Monitor Application

Continue monitoring for 30 minutes to ensure stability:

```bash
# Check logs periodically
aws logs tail /aws/ec2/khaleo-backend --follow

# Monitor metrics
aws cloudwatch get-metric-statistics \
  --namespace AWS/ApplicationELB \
  --metric-name HTTPCode_Target_5XX \
  --start-time $(date -u -d '30 minutes ago' +%Y-%m-%dT%H:%M:%S) \
  --end-time $(date -u +%Y-%m-%dT%H:%M:%S) \
  --period 300 \
  --statistics Sum
```

---

## Contact / Questions

If you encounter issues:
1. Check full documentation: `502_GATEWAY_FIX_COMPLETE.md`
2. Review logs: `docker logs -f khaleo-backend`
3. Verify schema: `DESC decks;` in RDS
4. Consult SOLUTION_SUMMARY.md for additional context

---

**Status: READY FOR EXECUTION**  
**Last Updated:** April 16, 2026  
**Estimated Downtime:** 2-3 minutes (during container restart)

