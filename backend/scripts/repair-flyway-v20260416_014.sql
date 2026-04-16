-- Quick local repair for failed migration V20260416.014
-- Use ONLY for local/staging where this migration failed partway.

USE khaleoapp;

-- Remove failed migration record so Flyway can re-run with fixed SQL.
DELETE FROM flyway_schema_history
WHERE version = '20260416.014'
  AND success = 0;

-- Optional visibility
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank DESC
LIMIT 10;

