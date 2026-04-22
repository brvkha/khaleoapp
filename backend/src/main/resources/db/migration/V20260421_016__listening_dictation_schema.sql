CREATE TABLE topics (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    description TEXT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'draft',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_topics_slug UNIQUE (slug)
);

CREATE TABLE exercises (
    id CHAR(36) PRIMARY KEY,
    topic_id CHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    order_index INT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'draft',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_exercises_topic FOREIGN KEY (topic_id) REFERENCES topics (id) ON DELETE CASCADE,
    CONSTRAINT uk_exercises_topic_slug UNIQUE (topic_id, slug),
    CONSTRAINT ck_exercises_order_index CHECK (order_index >= 1)
);

CREATE TABLE lessons (
    id CHAR(36) PRIMARY KEY,
    exercise_id CHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    order_index INT NOT NULL,
    media_url VARCHAR(2048) NULL,
    media_type VARCHAR(32) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'draft',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_lessons_exercise FOREIGN KEY (exercise_id) REFERENCES exercises (id) ON DELETE CASCADE,
    CONSTRAINT uk_lessons_exercise_slug UNIQUE (exercise_id, slug),
    CONSTRAINT ck_lessons_order_index CHECK (order_index >= 1)
);

CREATE TABLE sentences (
    id CHAR(36) PRIMARY KEY,
    lesson_id CHAR(36) NOT NULL,
    order_index INT NOT NULL,
    transcript LONGTEXT NOT NULL,
    translation LONGTEXT NULL,
    aliases_json LONGTEXT NULL,
    media_url VARCHAR(2048) NULL,
    start_time DECIMAL(10,3) NULL,
    end_time DECIMAL(10,3) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_sentences_lesson FOREIGN KEY (lesson_id) REFERENCES lessons (id) ON DELETE CASCADE,
    CONSTRAINT ck_sentences_order_index CHECK (order_index >= 1),
    CONSTRAINT ck_sentences_start_time CHECK (start_time IS NULL OR start_time >= 0),
    CONSTRAINT ck_sentences_end_time CHECK (end_time IS NULL OR start_time IS NULL OR end_time > start_time)
);

CREATE TABLE user_sentence_progress (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    sentence_id CHAR(36) NOT NULL,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    completion_source VARCHAR(32) NULL,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_sentence_progress_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_sentence_progress_sentence FOREIGN KEY (sentence_id) REFERENCES sentences (id) ON DELETE CASCADE,
    CONSTRAINT uk_user_sentence_progress UNIQUE (user_id, sentence_id)
);

CREATE INDEX idx_exercises_topic_order ON exercises (topic_id, order_index);
CREATE INDEX idx_lessons_exercise_order ON lessons (exercise_id, order_index);
CREATE INDEX idx_sentences_lesson_order ON sentences (lesson_id, order_index);
CREATE INDEX idx_user_sentence_progress_user_completed ON user_sentence_progress (user_id, is_completed);

