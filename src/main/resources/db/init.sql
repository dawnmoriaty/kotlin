-- =============================================
-- Financial App - PostgreSQL Initialization Script
-- Created for Ktor + Exposed + PostgreSQL
-- =============================================

-- Enable uuid-ossp extension (for UUID generation)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================
-- Table: users
-- =============================================
CREATE TABLE IF NOT EXISTS users (
                                     id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username TEXT NOT NULL UNIQUE,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT,

    -- Social logins (nullable)
    id_facebook TEXT UNIQUE,
    id_google TEXT UNIQUE,

    -- Role & status
    role TEXT NOT NULL DEFAULT 'user',
    is_blocked BOOLEAN NOT NULL DEFAULT false,

    -- Timestamps
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- Constraints
    CHECK (role IN ('admin', 'user')),
    CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CHECK (password_hash IS NOT NULL OR id_facebook IS NOT NULL OR id_google IS NOT NULL)
    );

-- =============================================
-- Table: profiles
-- =============================================
CREATE TABLE IF NOT EXISTS profiles (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    full_name TEXT,
    avatar_url TEXT,
    phone TEXT,
    date_of_birth DATE,
    address TEXT,
    bio TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =============================================
-- Table: categories
-- =============================================
CREATE TABLE IF NOT EXISTS categories (
                                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    type TEXT NOT NULL, -- 'income' or 'expense'
    icon TEXT,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_default BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    UNIQUE (user_id, name),
    CHECK (type IN ('income', 'expense'))
    );

-- =============================================
-- Table: transactions
-- =============================================
CREATE TABLE IF NOT EXISTS transactions (
                                            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    description TEXT NOT NULL,
    amount NUMERIC(18, 2) NOT NULL CHECK (amount > 0),
    transaction_date DATE NOT NULL,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

-- =============================================
-- Indexes for performance
-- =============================================

-- Users
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_social ON users(id_facebook, id_google);

-- Categories
CREATE INDEX IF NOT EXISTS idx_categories_user_type ON categories(user_id, type);

-- Transactions
CREATE INDEX IF NOT EXISTS idx_transactions_user_date ON transactions(user_id, transaction_date DESC);
CREATE INDEX IF NOT EXISTS idx_transactions_category ON transactions(category_id);
CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions(transaction_date);

-- =============================================
-- Trigger: auto-update updated_at
-- =============================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_profiles_updated_at
    BEFORE UPDATE ON profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_transactions_updated_at
    BEFORE UPDATE ON transactions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- Sample data (optional - for dev only)
-- =============================================

-- Admin user (password: Admin123!)
INSERT INTO users (username, email, password_hash, role)
VALUES (
           'admin',
           'admin@financial.app',
           '$2a$10$XqY8Z7VJ9Q6K5L4M3N2O1P0Q9R8S7T6U5V4W3X2Y1Z0aB9cD8eF7g', -- BCrypt hash placeholder
           'admin'
       ) ON CONFLICT DO NOTHING;

-- Test user with Google login
INSERT INTO users (username, email, id_google, role)
VALUES (
           'testuser_google',
           'testuser@gmail.com',
           '123456789012345678901',
           'user'
       ) ON CONFLICT DO NOTHING;

-- Insert profile for test user
INSERT INTO profiles (user_id, full_name, avatar_url)
SELECT id, 'Test User', 'https://example.com/avatar.jpg'
FROM users
WHERE email = 'testuser@gmail.com'
ON CONFLICT (user_id) DO NOTHING;

-- Default categories for test user
INSERT INTO categories (name, type, user_id, is_default)
SELECT 'Lương', 'income', id, true
FROM users WHERE email = 'testuser@gmail.com'
    ON CONFLICT DO NOTHING;

INSERT INTO categories (name, type, user_id, is_default)
SELECT 'Ăn uống', 'expense', id, true
FROM users WHERE email = 'testuser@gmail.com'
    ON CONFLICT DO NOTHING;

-- Sample transaction
INSERT INTO transactions (description, amount, transaction_date, category_id, user_id)
SELECT
    'Lương tháng 6/2025',
    15000000.00,
    '2025-06-01',
    c.id,
    u.id
FROM users u
         JOIN categories c ON c.user_id = u.id
WHERE u.email = 'testuser@gmail.com' AND c.name = 'Lương'
    ON CONFLICT DO NOTHING;

-- =============================================
-- Final message
-- =============================================
SELECT '✅ Financial App database initialized successfully!' AS status;