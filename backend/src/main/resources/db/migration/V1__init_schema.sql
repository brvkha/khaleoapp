CREATE TABLE users (
	id CHAR(36) NOT NULL,
	username VARCHAR(50) NOT NULL,
	email VARCHAR(320) NULL,
	password_hash VARCHAR(255) NOT NULL,
	role VARCHAR(32) NOT NULL DEFAULT 'ROLE_USER',
	is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
	daily_learning_limit INT NOT NULL DEFAULT 9999,
	timezone VARCHAR(64) NOT NULL DEFAULT 'Asia/Ho_Chi_Minh',
	failed_login_attempts INT NOT NULL DEFAULT 0,
	account_locked_until TIMESTAMP(6) NULL,
	banned_at TIMESTAMP(6) NULL,
	banned_by CHAR(36) NULL,
	created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
	updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
	CONSTRAINT pk_users PRIMARY KEY (id),
	CONSTRAINT uk_users_username UNIQUE (username),
	CONSTRAINT uk_users_email UNIQUE (email),
	CONSTRAINT fk_users_banned_by FOREIGN KEY (banned_by) REFERENCES users (id),
	CONSTRAINT ck_users_username_not_blank CHECK (TRIM(username) <> ''),
	CONSTRAINT ck_users_username_normalized CHECK (username = LOWER(TRIM(username))),
	CONSTRAINT ck_users_daily_learning_limit CHECK (daily_learning_limit BETWEEN 1 AND 9999),
	CONSTRAINT ck_users_failed_login_attempts_non_negative CHECK (failed_login_attempts >= 0)
);

CREATE TABLE decks (
	id CHAR(36) NOT NULL,
	author_id CHAR(36) NOT NULL,
	parent_id CHAR(36) NULL,
	name VARCHAR(100) NOT NULL,
	description TEXT NULL,
	cover_image_url VARCHAR(2048) NULL,
	tags TEXT NULL,
	is_public BOOLEAN NOT NULL DEFAULT FALSE,
	banned_at TIMESTAMP(6) NULL,
	banned_by CHAR(36) NULL,
	created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
	updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
	CONSTRAINT pk_decks PRIMARY KEY (id),
	CONSTRAINT fk_decks_author FOREIGN KEY (author_id) REFERENCES users (id),
	CONSTRAINT fk_deck_parent FOREIGN KEY (parent_id) REFERENCES decks (id) ON DELETE CASCADE,
	CONSTRAINT fk_decks_banned_by FOREIGN KEY (banned_by) REFERENCES users (id)
);

CREATE TABLE cards (
	id CHAR(36) NOT NULL,
	deck_id CHAR(36) NOT NULL,
	front_text TEXT NULL,
	front_media_url VARCHAR(2048) NULL,
	back_text TEXT NULL,
	back_media_url VARCHAR(2048) NULL,
	image_url VARCHAR(2048) NULL,
	part_of_speech VARCHAR(64) NULL,
	phonetic VARCHAR(255) NULL,
	examples_json LONGTEXT NOT NULL,
	version BIGINT NOT NULL DEFAULT 0,
	created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
	updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
	CONSTRAINT pk_cards PRIMARY KEY (id),
	CONSTRAINT fk_cards_deck FOREIGN KEY (deck_id) REFERENCES decks (id),
	CONSTRAINT ck_cards_front_content CHECK (
		(front_text IS NOT NULL AND TRIM(front_text) <> '') OR
		(front_media_url IS NOT NULL AND TRIM(front_media_url) <> '')
	),
	CONSTRAINT ck_cards_back_content CHECK (
		(back_text IS NOT NULL AND TRIM(back_text) <> '') OR
		(back_media_url IS NOT NULL AND TRIM(back_media_url) <> '')
	)
);

CREATE TABLE card_learning_states (
	id CHAR(36) NOT NULL,
	card_id CHAR(36) NOT NULL,
	user_id CHAR(36) NOT NULL,
	state VARCHAR(16) NOT NULL DEFAULT 'NEW',
	ease_factor DECIMAL(5,2) NOT NULL DEFAULT 2.50,
	interval_in_days INT NOT NULL DEFAULT 0,
	next_review_date TIMESTAMP(6) NULL,
	last_reviewed_at TIMESTAMP(6) NULL,
	learning_step_good_count INT NOT NULL DEFAULT 0,
	fsrs_stability DECIMAL(10,4) NOT NULL DEFAULT 0.0000,
	fsrs_difficulty DECIMAL(5,2) NOT NULL DEFAULT 0.00,
	fsrs_elapsed_days INT NOT NULL DEFAULT 0,
	fsrs_scheduled_days INT NOT NULL DEFAULT 0,
	fsrs_reps INT NOT NULL DEFAULT 0,
	fsrs_lapses INT NOT NULL DEFAULT 0,
	version BIGINT NOT NULL DEFAULT 0,
	created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
	updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
	CONSTRAINT pk_card_learning_states PRIMARY KEY (id),
	CONSTRAINT uk_learning_state_user_card UNIQUE (user_id, card_id),
	CONSTRAINT fk_learning_states_card FOREIGN KEY (card_id) REFERENCES cards (id),
	CONSTRAINT fk_learning_states_user FOREIGN KEY (user_id) REFERENCES users (id),
	CONSTRAINT ck_learning_states_state CHECK (state IN ('NEW', 'LEARNING', 'RELEARNING', 'MASTERED', 'REVIEW')),
	CONSTRAINT ck_learning_states_ease_factor_positive CHECK (ease_factor > 0),
	CONSTRAINT ck_learning_states_interval_non_negative CHECK (interval_in_days >= 0),
	CONSTRAINT ck_learning_step_good_count_non_negative CHECK (learning_step_good_count >= 0),
	CONSTRAINT ck_learning_states_fsrs_elapsed_non_negative CHECK (fsrs_elapsed_days >= 0),
	CONSTRAINT ck_learning_states_fsrs_scheduled_non_negative CHECK (fsrs_scheduled_days >= 0),
	CONSTRAINT ck_learning_states_fsrs_reps_non_negative CHECK (fsrs_reps >= 0),
	CONSTRAINT ck_learning_states_fsrs_lapses_non_negative CHECK (fsrs_lapses >= 0)
);

CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_decks_author_id ON decks (author_id);
CREATE INDEX idx_decks_banned_at ON decks (banned_at);
CREATE INDEX idx_cards_deck_id ON cards (deck_id);
CREATE INDEX idx_learning_states_card_id ON card_learning_states (card_id);
CREATE INDEX idx_learning_states_user_id ON card_learning_states (user_id);
CREATE INDEX idx_learning_states_state_next_review_date ON card_learning_states (state, next_review_date);
