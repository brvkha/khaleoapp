
ALTER TABLE decks ADD COLUMN parent_id CHAR(36) NULL;
ALTER TABLE decks ADD CONSTRAINT fk_deck_parent FOREIGN KEY (parent_id) REFERENCES decks(id) ON DELETE CASCADE;
-- Default IELTS Parent Deck
INSERT INTO decks (id, author_id, name, description, is_public, tags, parent_id, created_at, updated_at)
VALUES ('550e8400-e29b-41d4-a716-446655440000', '00000000-0000-0000-0000-000000000002', 'IELTS', 'IELTS Vocabulary Collection', TRUE, 'ielts,vocabulary', NULL, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));
-- Default Sub-deck for Vocabulary
INSERT INTO decks (id, author_id, name, parent_id, description, is_public, tags, created_at, updated_at)
VALUES ('550e8400-e29b-41d4-a716-446655440001', '00000000-0000-0000-0000-000000000002', 'Vocabulary List 1', '550e8400-e29b-41d4-a716-446655440000', 'Extracted from Excel', TRUE, 'vocabulary,words', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));
