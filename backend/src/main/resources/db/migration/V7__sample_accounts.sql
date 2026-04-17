INSERT INTO users (id, username, email, password_hash, role, is_email_verified, daily_learning_limit, timezone, failed_login_attempts)
SELECT '00000000-0000-0000-0000-000000000002', 'admin', 'admin@gmail.com', '$2b$12$2UaQlqvE0pKiOO8ud5YDy.qOgSu.r5YWFMmpMicS/F0I3WxYdM366', 'ROLE_ADMIN', TRUE, 9999, 'Asia/Ho_Chi_Minh', 0
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO users (id, username, email, password_hash, role, is_email_verified, daily_learning_limit, timezone, failed_login_attempts)
SELECT '00000000-0000-0000-0000-000000000003', 'khaleo', 'khaleo@gmail.com', '$2b$12$2UaQlqvE0pKiOO8ud5YDy.qOgSu.r5YWFMmpMicS/F0I3WxYdM366', 'ROLE_USER', TRUE, 9999, 'Asia/Ho_Chi_Minh', 0
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'khaleo');
