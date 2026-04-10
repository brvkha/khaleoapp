INSERT INTO users (
    id,
    email,
    password_hash,
    role,
    is_email_verified,
    daily_learning_limit,
    failed_login_attempts,
    account_locked_until,
    banned_at,
    banned_by,
    created_at,
    updated_at
)
SELECT
    '00000000-0000-0000-0000-000000000001',
    'user@test.com',
    '$2b$10$DEgHKnmj9mF9xnvKR9rZqOA8Zfxb7fB0quF0yVf4ohdJ.CqA78jW.',
    'ROLE_USER',
    TRUE,
    9999,
    0,
    NULL,
    NULL,
    NULL,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'user@test.com'
);

