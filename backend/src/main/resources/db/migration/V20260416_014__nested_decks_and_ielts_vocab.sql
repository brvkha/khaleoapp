-- Idempotent nested-deck migration: safe for reruns and partially applied environments.
ALTER TABLE decks ADD COLUMN parent_id CHAR(36) NULL AFTER id;

SET @fk_exists := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'decks'
      AND CONSTRAINT_NAME = 'fk_deck_parent'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);

SET @fk_sql := IF(
    @fk_exists = 0,
    'ALTER TABLE decks ADD CONSTRAINT fk_deck_parent FOREIGN KEY (parent_id) REFERENCES decks(id) ON DELETE CASCADE',
    'SELECT 1'
);

PREPARE fk_stmt FROM @fk_sql;
EXECUTE fk_stmt;
DEALLOCATE PREPARE fk_stmt;

-- Seed default IELTS parent deck only when owner user exists and row is missing.
INSERT INTO decks (id, author_id, name, description, is_public, tags, parent_id, created_at, updated_at)
SELECT
    '550e8400-e29b-41d4-a716-446655440000',
    '00000000-0000-0000-0000-000000000002',
    'IELTS',
    'IELTS Vocabulary Collection',
    TRUE,
    'ielts,vocabulary',
    NULL,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
FROM users u
WHERE u.id = '00000000-0000-0000-0000-000000000002'
  AND NOT EXISTS (
      SELECT 1 FROM decks d WHERE d.id = '550e8400-e29b-41d4-a716-446655440000'
  );

-- Seed default IELTS child deck only when parent exists and row is missing.
INSERT INTO decks (id, author_id, name, parent_id, description, is_public, tags, created_at, updated_at)
SELECT
    '550e8400-e29b-41d4-a716-446655440001',
    '00000000-0000-0000-0000-000000000002',
    'Vocabulary List 1',
    '550e8400-e29b-41d4-a716-446655440000',
    'Extracted from Excel',
    TRUE,
    'vocabulary,words',
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
FROM decks parent
WHERE parent.id = '550e8400-e29b-41d4-a716-446655440000'
  AND NOT EXISTS (
      SELECT 1 FROM decks d WHERE d.id = '550e8400-e29b-41d4-a716-446655440001'
  );
