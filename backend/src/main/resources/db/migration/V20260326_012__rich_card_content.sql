DROP PROCEDURE IF EXISTS migrate_rich_card_content;

DELIMITER $$
CREATE PROCEDURE migrate_rich_card_content()
BEGIN
    -- No-op on clean installs.
    -- Rich-card columns (image_url, part_of_speech, phonetic, examples_json, version)
    -- are part of V1__init_schema.sql canonical schema.
END$$
DELIMITER ;

CALL migrate_rich_card_content();
DROP PROCEDURE IF EXISTS migrate_rich_card_content;
