-- Idempotent nested-deck migration: safe for reruns and partially applied environments.

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
