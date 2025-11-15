-- =============================================
-- Migration: Add Budget, Recurring Transactions, and Debt/Loan tables
-- =============================================

-- =============================================
-- Table: budgets - Ngân sách cho từng category
-- =============================================
CREATE TABLE IF NOT EXISTS budgets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    amount NUMERIC(18, 2) NOT NULL CHECK (amount > 0),
    period TEXT NOT NULL DEFAULT 'monthly', -- 'monthly', 'weekly', 'yearly'
    start_date DATE NOT NULL,
    end_date DATE,
    is_active BOOLEAN NOT NULL DEFAULT true,
    alert_percentage NUMERIC(5, 2) DEFAULT 80.00, -- Alert when spent 80% of budget
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    UNIQUE (user_id, category_id, period),
    CHECK (period IN ('daily', 'weekly', 'monthly', 'yearly')),
    CHECK (alert_percentage > 0 AND alert_percentage <= 100),
    CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE INDEX idx_budgets_user_id ON budgets(user_id);
CREATE INDEX idx_budgets_category_id ON budgets(category_id);
CREATE INDEX idx_budgets_period ON budgets(period);
CREATE INDEX idx_budgets_active ON budgets(is_active);

-- =============================================
-- Table: recurring_transactions - Giao dịch lặp lại
-- =============================================
CREATE TABLE IF NOT EXISTS recurring_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    description TEXT NOT NULL,
    amount NUMERIC(18, 2) NOT NULL CHECK (amount > 0),
    frequency TEXT NOT NULL, -- 'daily', 'weekly', 'monthly', 'yearly'
    start_date DATE NOT NULL,
    end_date DATE,
    next_occurrence DATE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    auto_create BOOLEAN NOT NULL DEFAULT true, -- Tự động tạo transaction
    day_of_month INTEGER, -- Ngày trong tháng (1-31) cho monthly
    day_of_week INTEGER, -- Ngày trong tuần (0-6, 0=Sunday) cho weekly
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CHECK (frequency IN ('daily', 'weekly', 'monthly', 'yearly')),
    CHECK (end_date IS NULL OR end_date >= start_date),
    CHECK (day_of_month IS NULL OR (day_of_month >= 1 AND day_of_month <= 31)),
    CHECK (day_of_week IS NULL OR (day_of_week >= 0 AND day_of_week <= 6))
);

CREATE INDEX idx_recurring_transactions_user_id ON recurring_transactions(user_id);
CREATE INDEX idx_recurring_transactions_next_occurrence ON recurring_transactions(next_occurrence);
CREATE INDEX idx_recurring_transactions_active ON recurring_transactions(is_active);
CREATE INDEX idx_recurring_transactions_auto_create ON recurring_transactions(auto_create);

-- =============================================
-- Table: debts - Quản lý nợ/cho vay
-- =============================================
CREATE TABLE IF NOT EXISTS debts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type TEXT NOT NULL, -- 'borrowed' (tôi vay/nợ người khác), 'lent' (tôi cho vay)
    person_name TEXT NOT NULL,
    person_contact TEXT, -- Phone/email
    amount NUMERIC(18, 2) NOT NULL CHECK (amount > 0),
    remaining_amount NUMERIC(18, 2) NOT NULL CHECK (remaining_amount >= 0),
    interest_rate NUMERIC(5, 2) DEFAULT 0, -- Lãi suất (%)
    description TEXT,
    due_date DATE,
    status TEXT NOT NULL DEFAULT 'active', -- 'active', 'partial', 'paid', 'overdue'
    start_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CHECK (type IN ('borrowed', 'lent')),
    CHECK (status IN ('active', 'partial', 'paid', 'overdue')),
    CHECK (remaining_amount <= amount),
    CHECK (interest_rate >= 0)
);

CREATE INDEX idx_debts_user_id ON debts(user_id);
CREATE INDEX idx_debts_type ON debts(type);
CREATE INDEX idx_debts_status ON debts(status);
CREATE INDEX idx_debts_due_date ON debts(due_date);

-- =============================================
-- Table: debt_payments - Lịch sử thanh toán nợ
-- =============================================
CREATE TABLE IF NOT EXISTS debt_payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    debt_id UUID NOT NULL REFERENCES debts(id) ON DELETE CASCADE,
    amount NUMERIC(18, 2) NOT NULL CHECK (amount > 0),
    payment_date DATE NOT NULL DEFAULT CURRENT_DATE,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CHECK (payment_date <= CURRENT_DATE)
);

CREATE INDEX idx_debt_payments_debt_id ON debt_payments(debt_id);
CREATE INDEX idx_debt_payments_date ON debt_payments(payment_date);

-- =============================================
-- Triggers for updated_at
-- =============================================

CREATE TRIGGER trg_budgets_updated_at
    BEFORE UPDATE ON budgets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_recurring_transactions_updated_at
    BEFORE UPDATE ON recurring_transactions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_debts_updated_at
    BEFORE UPDATE ON debts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- Function: Update debt status automatically
-- =============================================
CREATE OR REPLACE FUNCTION update_debt_status()
RETURNS TRIGGER AS $$
BEGIN
    -- Update remaining amount
    NEW.remaining_amount := NEW.amount - COALESCE(
        (SELECT SUM(amount) FROM debt_payments WHERE debt_id = NEW.id),
        0
    );

    -- Update status based on remaining amount and due date
    IF NEW.remaining_amount <= 0 THEN
        NEW.status := 'paid';
    ELSIF NEW.remaining_amount < NEW.amount THEN
        NEW.status := 'partial';
    ELSIF NEW.due_date IS NOT NULL AND NEW.due_date < CURRENT_DATE THEN
        NEW.status := 'overdue';
    ELSE
        NEW.status := 'active';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_debt_status
    BEFORE INSERT OR UPDATE ON debts
    FOR EACH ROW EXECUTE FUNCTION update_debt_status();

-- =============================================
-- Function: Update debt after payment
-- =============================================
CREATE OR REPLACE FUNCTION update_debt_after_payment()
RETURNS TRIGGER AS $$
BEGIN
    -- Update debt's remaining amount and status
    UPDATE debts
    SET remaining_amount = amount - COALESCE(
            (SELECT SUM(amount) FROM debt_payments WHERE debt_id = NEW.debt_id),
            0
        ),
        updated_at = NOW()
    WHERE id = NEW.debt_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_debt_payment_insert
    AFTER INSERT ON debt_payments
    FOR EACH ROW EXECUTE FUNCTION update_debt_after_payment();

-- =============================================
-- Sample data (for testing)
-- =============================================

-- Sample budget (Ngân sách ăn uống 5 triệu/tháng)
-- INSERT INTO budgets (user_id, category_id, amount, period, start_date, alert_percentage)
-- SELECT u.id, c.id, 5000000.00, 'monthly', CURRENT_DATE, 80.00
-- FROM users u
-- JOIN categories c ON c.user_id = u.id
-- WHERE u.email = 'test@example.com' AND c.name = 'Ăn uống'
-- LIMIT 1;

-- Sample recurring transaction (Tiền nhà 3 triệu mỗi tháng)
-- INSERT INTO recurring_transactions (user_id, category_id, description, amount, frequency, start_date, next_occurrence, day_of_month)
-- SELECT u.id, c.id, 'Tiền nhà tháng', 3000000.00, 'monthly', CURRENT_DATE, CURRENT_DATE, 1
-- FROM users u
-- JOIN categories c ON c.user_id = u.id
-- WHERE u.email = 'test@example.com' AND c.type = 'expense'
-- LIMIT 1;

-- =============================================
-- Views for convenience
-- =============================================

-- View: Budget spending summary
CREATE OR REPLACE VIEW budget_spending_summary AS
SELECT
    b.id as budget_id,
    b.user_id,
    b.category_id,
    c.name as category_name,
    b.amount as budget_amount,
    b.period,
    b.start_date,
    b.end_date,
    COALESCE(SUM(t.amount), 0) as spent_amount,
    b.amount - COALESCE(SUM(t.amount), 0) as remaining_amount,
    CASE
        WHEN b.amount > 0 THEN ROUND((COALESCE(SUM(t.amount), 0) / b.amount * 100)::numeric, 2)
        ELSE 0
    END as spent_percentage,
    CASE
        WHEN COALESCE(SUM(t.amount), 0) >= b.amount THEN true
        ELSE false
    END as is_exceeded,
    CASE
        WHEN COALESCE(SUM(t.amount), 0) >= (b.amount * b.alert_percentage / 100) THEN true
        ELSE false
    END as should_alert
FROM budgets b
JOIN categories c ON b.category_id = c.id
LEFT JOIN transactions t ON t.category_id = b.category_id
    AND t.user_id = b.user_id
    AND t.transaction_date >= b.start_date
    AND (b.end_date IS NULL OR t.transaction_date <= b.end_date)
WHERE b.is_active = true
GROUP BY b.id, b.user_id, b.category_id, c.name, b.amount, b.period, b.start_date, b.end_date, b.alert_percentage;

-- View: Active debts summary
CREATE OR REPLACE VIEW active_debts_summary AS
SELECT
    d.id,
    d.user_id,
    d.type,
    d.person_name,
    d.amount as original_amount,
    d.remaining_amount,
    d.amount - d.remaining_amount as paid_amount,
    CASE
        WHEN d.amount > 0 THEN ROUND(((d.amount - d.remaining_amount) / d.amount * 100)::numeric, 2)
        ELSE 0
    END as paid_percentage,
    d.interest_rate,
    d.due_date,
    d.status,
    CASE
        WHEN d.due_date IS NOT NULL AND d.due_date < CURRENT_DATE AND d.status != 'paid' THEN
            CURRENT_DATE - d.due_date
        ELSE 0
    END as days_overdue,
    COUNT(dp.id) as payment_count,
    MAX(dp.payment_date) as last_payment_date
FROM debts d
LEFT JOIN debt_payments dp ON dp.debt_id = d.id
WHERE d.status != 'paid'
GROUP BY d.id, d.user_id, d.type, d.person_name, d.amount, d.remaining_amount, d.interest_rate, d.due_date, d.status;

-- =============================================
-- Grant permissions
-- =============================================

ALTER TABLE budgets OWNER TO root;
ALTER TABLE recurring_transactions OWNER TO root;
ALTER TABLE debts OWNER TO root;
ALTER TABLE debt_payments OWNER TO root;

-- =============================================
-- Final message
-- =============================================
SELECT '✅ Budget, Recurring Transactions, and Debt/Loan tables created successfully!' AS status;

