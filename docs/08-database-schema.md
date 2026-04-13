# 08 - Database Schema (Flyway Migration)

## V1__init_tables.sql

```sql
-- Users table
CREATE TABLE users (
  id CHAR(36) PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role ENUM('USER', 'ADMIN') DEFAULT 'USER',
  verified BOOLEAN DEFAULT FALSE,
  banned BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_email (email),
  INDEX idx_created_at (created_at)
);

-- Decks table
CREATE TABLE decks (
  id CHAR(36) PRIMARY KEY,
  user_id CHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  is_public BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_user_id (user_id),
  INDEX idx_created_at (created_at)
);

-- Cards table
CREATE TABLE cards (
  id CHAR(36) PRIMARY KEY,
  deck_id CHAR(36) NOT NULL,
  term VARCHAR(500) NOT NULL,
  answer LONGTEXT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  version BIGINT DEFAULT 0,
  FOREIGN KEY (deck_id) REFERENCES decks(id) ON DELETE CASCADE,
  INDEX idx_deck_id (deck_id),
  INDEX idx_created_at (created_at)
);

-- Card Learning States (FSRS v6)
CREATE TABLE card_learning_states (
  id CHAR(36) PRIMARY KEY,
  user_id CHAR(36) NOT NULL,
  card_id CHAR(36) NOT NULL,
  state ENUM('NEW','LEARNING','REVIEW','RELEARNING','MASTERED') DEFAULT 'NEW',
  fsrs_stability DECIMAL(10,4) DEFAULT 0,
  fsrs_difficulty DECIMAL(5,2) DEFAULT 0,
  fsrs_reps INT DEFAULT 0,
  fsrs_lapses INT DEFAULT 0,
  fsrs_elapsed_days INT DEFAULT 0,
  fsrs_scheduled_days INT DEFAULT 0,
  learning_step_good_count INT DEFAULT 0,
  last_reviewed_at TIMESTAMP NULL,
  next_review_at TIMESTAMP NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  version BIGINT DEFAULT 0,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
  UNIQUE KEY uk_user_card (user_id, card_id),
  INDEX idx_user_id_next_review (user_id, next_review_at, state),
  INDEX idx_deck_user_due (user_id, next_review_at, state)
);

-- Refresh Tokens
CREATE TABLE refresh_tokens (
  id CHAR(36) PRIMARY KEY,
  user_id CHAR(36) NOT NULL,
  token_hash VARCHAR(255) NOT NULL UNIQUE,
  expires_at TIMESTAMP NOT NULL,
  revoked BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_user_id (user_id),
  INDEX idx_expires_at (expires_at)
);

-- Login Attempts (Brute force detection)
CREATE TABLE login_attempts (
  id CHAR(36) PRIMARY KEY,
  email VARCHAR(255),
  success BOOLEAN DEFAULT FALSE,
  ip_address VARCHAR(45),
  user_agent TEXT,
  attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_email_attempted_at (email, attempted_at)
);
```

## Key Indexes

- `card_learning_states(user_id, next_review_at, state)` - tối ưu query due cards
- `cards(deck_id)` - query card trong deck
- `refresh_tokens(user_id, expires_at)` - cleanup token hết hạn
- `login_attempts(email, attempted_at)` - detect brute force login

## Migration Strategy

- Dùng Flyway version control (V1, V2, ...)
- Đặt tại `src/main/resources/db/migration/`
- Spring Boot auto-migrate khi startup
- Có thể rollback nếu cần

