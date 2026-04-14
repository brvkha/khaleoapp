
ALTER TABLE decks ADD COLUMN parent_id CHAR(36) NULL;
ALTER TABLE decks ADD CONSTRAINT fk_deck_parent FOREIGN KEY (parent_id) REFERENCES decks(id) ON DELETE CASCADE;
-- Default IELTS Parent Deck
INSERT INTO decks (id, user_id, title, description, visibility, created_at, updated_at) 
VALUES ('ielts-parent-deck-0000-00000000000', '00000000-0000-0000-0000-000000000002', 'IELTS', 'IELTS Vocabulary Collection', 'PUBLIC', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));
-- Default Sub-deck for Vocabulary
INSERT INTO decks (id, user_id, title, parent_id, description, visibility, created_at, updated_at) 
VALUES ('ielts-subdeck-vocab-0000-00000000000', '00000000-0000-0000-0000-000000000002', 'Vocabulary List 1', 'ielts-parent-deck-0000-00000000000', 'Extracted from Excel', 'PUBLIC', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));
