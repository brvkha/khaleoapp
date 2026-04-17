CREATE TABLE refresh_tokens (
    id CHAR(36) NOT NULL,
    token VARCHAR(512) NOT NULL,
    user_id CHAR(36) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    revoked_at TIMESTAMP(6) NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_token UNIQUE (token),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT ck_refresh_tokens_expiry_future CHECK (expires_at > created_at)
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
