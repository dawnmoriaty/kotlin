-- =============================================
-- SQL Helper Scripts for Testing Forgot Password
-- =============================================

-- 1. Check if password_reset_tokens table exists
SELECT
    table_name,
    column_name,
    data_type
FROM information_schema.columns
WHERE table_name = 'password_reset_tokens'
ORDER BY ordinal_position;

-- =============================================
-- 2. View all reset tokens (for debugging)
-- =============================================
SELECT
    id,
    user_id,
    LEFT(token, 20) || '...' as token_preview,
    expires_at,
    is_used,
    CASE
        WHEN expires_at < NOW() THEN 'EXPIRED'
        WHEN is_used THEN 'USED'
        ELSE 'VALID'
    END as status,
    created_at,
    EXTRACT(EPOCH FROM (expires_at - NOW())) / 60 as minutes_until_expire
FROM password_reset_tokens
ORDER BY created_at DESC;

-- =============================================
-- 3. View user with reset tokens
-- =============================================
SELECT
    u.id as user_id,
    u.username,
    u.email,
    COUNT(prt.id) as total_tokens,
    COUNT(CASE WHEN prt.is_used = false AND prt.expires_at > NOW() THEN 1 END) as valid_tokens
FROM users u
LEFT JOIN password_reset_tokens prt ON u.id = prt.user_id
GROUP BY u.id, u.username, u.email
ORDER BY u.created_at DESC;

-- =============================================
-- 4. Get latest valid token for a user (for testing)
-- =============================================
-- Replace 'user@example.com' with your test email
SELECT
    token,
    expires_at,
    is_used,
    created_at,
    EXTRACT(EPOCH FROM (expires_at - NOW())) / 60 as minutes_until_expire
FROM password_reset_tokens prt
JOIN users u ON prt.user_id = u.id
WHERE u.email = 'user@example.com'
  AND prt.is_used = false
  AND prt.expires_at > NOW()
ORDER BY prt.created_at DESC
LIMIT 1;

-- =============================================
-- 5. Clean up expired tokens
-- =============================================
DELETE FROM password_reset_tokens
WHERE expires_at < NOW() OR is_used = true;

-- Show deleted count
SELECT 'Deleted ' || COUNT(*) || ' expired/used tokens' as result
FROM password_reset_tokens
WHERE expires_at < NOW() OR is_used = true;

-- =============================================
-- 6. Clean up all tokens for testing
-- =============================================
-- WARNING: This deletes ALL reset tokens
-- TRUNCATE password_reset_tokens;
-- SELECT 'All reset tokens deleted' as result;

-- =============================================
-- 7. Create test user for forgot password testing
-- =============================================
-- Only run if you need a test user
INSERT INTO users (username, email, password_hash, role)
VALUES (
    'testforgot',
    'testforgot@example.com',
    '$2a$10$XqY8Z7VJ9Q6K5L4M3N2O1P0Q9R8S7T6U5V4W3X2Y1Z0aB9cD8eF7g', -- BCrypt for "Password123!"
    'user'
)
ON CONFLICT (email) DO NOTHING
RETURNING id, username, email;

-- =============================================
-- 8. Check user's OAuth accounts
-- =============================================
SELECT
    id,
    username,
    email,
    password_hash IS NOT NULL as has_password,
    id_google IS NOT NULL as has_google,
    id_facebook IS NOT NULL as has_facebook,
    role,
    created_at
FROM users
ORDER BY created_at DESC
LIMIT 10;

-- =============================================
-- 9. Manually create a test token (for testing without email)
-- =============================================
-- Replace USER_ID with actual user UUID
-- This is useful when email is not configured
INSERT INTO password_reset_tokens (user_id, token, expires_at, is_used)
SELECT
    id,
    'test-token-' || gen_random_uuid()::text,
    NOW() + INTERVAL '15 minutes',
    false
FROM users
WHERE email = 'testforgot@example.com'
RETURNING token, expires_at;

-- =============================================
-- 10. Statistics - Reset password usage
-- =============================================
SELECT
    DATE(created_at) as date,
    COUNT(*) as total_requests,
    COUNT(CASE WHEN is_used THEN 1 END) as completed_resets,
    COUNT(CASE WHEN NOT is_used AND expires_at < NOW() THEN 1 END) as expired_unused,
    ROUND(COUNT(CASE WHEN is_used THEN 1 END)::numeric / COUNT(*)::numeric * 100, 2) as completion_rate
FROM password_reset_tokens
GROUP BY DATE(created_at)
ORDER BY date DESC;

-- =============================================
-- 11. Find users who never reset password
-- =============================================
SELECT
    u.id,
    u.username,
    u.email,
    u.created_at,
    COUNT(prt.id) as reset_attempts,
    MAX(prt.created_at) as last_reset_request
FROM users u
LEFT JOIN password_reset_tokens prt ON u.id = prt.user_id
WHERE u.password_hash IS NOT NULL  -- Only users with password
GROUP BY u.id, u.username, u.email, u.created_at
HAVING COUNT(prt.id) = 0
ORDER BY u.created_at DESC;

-- =============================================
-- 12. Monitor suspicious activity
-- =============================================
-- Users with many reset requests (potential abuse)
SELECT
    u.email,
    COUNT(*) as reset_requests,
    MIN(prt.created_at) as first_request,
    MAX(prt.created_at) as last_request,
    EXTRACT(EPOCH FROM (MAX(prt.created_at) - MIN(prt.created_at))) / 3600 as hours_span
FROM users u
JOIN password_reset_tokens prt ON u.id = prt.user_id
WHERE prt.created_at > NOW() - INTERVAL '24 hours'
GROUP BY u.email
HAVING COUNT(*) > 5
ORDER BY reset_requests DESC;

-- =============================================
-- 13. Quick test query - Get user with password
-- =============================================
SELECT
    id,
    username,
    email,
    'Password123!' as test_password,
    password_hash IS NOT NULL as has_password
FROM users
WHERE password_hash IS NOT NULL
  AND email NOT LIKE '%google%'
  AND email NOT LIKE '%facebook%'
LIMIT 1;

-- =============================================
-- 14. Validate token manually
-- =============================================
-- Replace 'YOUR_TOKEN' with actual token
SELECT
    prt.token,
    u.email,
    u.username,
    prt.expires_at,
    prt.is_used,
    prt.created_at,
    CASE
        WHEN prt.is_used THEN 'ERROR: Token already used'
        WHEN prt.expires_at < NOW() THEN 'ERROR: Token expired'
        ELSE 'OK: Token is valid'
    END as validation_status,
    EXTRACT(EPOCH FROM (prt.expires_at - NOW())) / 60 as minutes_remaining
FROM password_reset_tokens prt
JOIN users u ON prt.user_id = u.id
WHERE prt.token = 'YOUR_TOKEN';

-- =============================================
-- 15. Reset test environment
-- =============================================
-- WARNING: This deletes all test data
-- ONLY USE IN DEVELOPMENT!

-- Delete all reset tokens
-- DELETE FROM password_reset_tokens;

-- Delete test users
-- DELETE FROM users WHERE email LIKE 'test%' OR username LIKE 'test%';

-- Verify cleanup
-- SELECT 'Reset tokens: ' || COUNT(*) FROM password_reset_tokens;
-- SELECT 'Test users: ' || COUNT(*) FROM users WHERE email LIKE 'test%';

