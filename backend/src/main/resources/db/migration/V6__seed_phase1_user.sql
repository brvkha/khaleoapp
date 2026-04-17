INSERT INTO users (
    id,
    username,
    email,
    password_hash,
    role,
    is_email_verified,
    daily_learning_limit,
    timezone,
    failed_login_attempts,
    account_locked_until,
    banned_at,
    banned_by,
    created_at,
    updated_at
)
SELECT
    '00000000-0000-0000-0000-000000000001',
    'user',
    'user@gmail.com',
    '$2b$12$2UaQlqvE0pKiOO8ud5YDy.qOgSu.r5YWFMmpMicS/F0I3WxYdM366',
    'ROLE_USER',
    TRUE,
    9999,
    'Asia/Ho_Chi_Minh',
    0,
    NULL,
    NULL,
    NULL,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'user'
);
