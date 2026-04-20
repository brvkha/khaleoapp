ALTER TABLE cards
    ADD COLUMN front_content TEXT NULL AFTER front_text,
    ADD COLUMN back_content TEXT NULL AFTER back_text,
    ADD COLUMN search_text TEXT NULL AFTER back_content;

UPDATE cards
SET front_content = COALESCE(NULLIF(TRIM(front_text), ''), '<p></p>'),
    back_content = COALESCE(NULLIF(TRIM(back_text), ''), '<p></p>')
WHERE front_content IS NULL OR back_content IS NULL;

UPDATE cards
SET search_text = LOWER(TRIM(CONCAT_WS(' ',
    COALESCE(front_text, ''),
    COALESCE(back_text, '')
)))
WHERE search_text IS NULL;

ALTER TABLE cards
    MODIFY front_content TEXT NOT NULL,
    MODIFY back_content TEXT NOT NULL,
    MODIFY search_text TEXT NOT NULL;

