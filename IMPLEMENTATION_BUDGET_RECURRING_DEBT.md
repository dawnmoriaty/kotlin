# 🎯 IMPLEMENTATION GUIDE - Budget, Recurring Transactions & Debt Management

## ✅ Đã hoàn thành (Phase 1)

### 1. Database Schema
- ✅ Migration SQL với 4 bảng mới:
  - `budgets` - Quản lý ngân sách
  - `recurring_transactions` - Giao dịch lặp lại
  - `debts` - Quản lý nợ/cho vay
  - `debt_payments` - Lịch sử thanh toán nợ
- ✅ Triggers tự động
- ✅ Views cho reporting
- ✅ Indexes cho performance

### 2. Exposed Tables
- ✅ `Budgets`, `RecurringTransactions`, `Debts`, `DebtPayments`

### 3. Models
- ✅ `Budget`, `BudgetSpending`
- ✅ `RecurringTransaction`
- ✅ `Debt`, `DebtPayment`, `DebtSummary`

### 4. DTOs
- ✅ Request DTOs cho CRUD operations
- ✅ Response DTOs với thống kê

### 5. Repository Interfaces
- ✅ `IBudgetRepository`
- ✅ `IRecurringTransactionRepository`
- ✅ `IDebtRepository`, `IDebtPaymentRepository`

---

## 📋 TODO - Implementation Steps

### Step 1: Run Migration
```bash
psql -U root -d financial_db_dev -f migration_budget_recurring_debt.sql
```

### Step 2: Update DatabaseFactory.kt
Thêm tables mới vào SchemaUtils.create:
```kotlin
SchemaUtils.create(
    Users, Profiles, Categories, Transactions, RefreshTokens, PasswordResetTokens,
    Budgets, RecurringTransactions, Debts, DebtPayments  // <-- ADD THIS
)
```

### Step 3: Implement Repositories
Cần tạo 4 repository implementations:
- `BudgetRepository.kt`
- `RecurringTransactionRepository.kt`
- `DebtRepository.kt`
- `DebtPaymentRepository.kt`

### Step 4: Implement Services
Cần tạo 3 services:
- `BudgetService.kt` - Logic ngân sách
- `RecurringTransactionService.kt` - Logic giao dịch lặp + scheduler
- `DebtService.kt` - Logic nợ/cho vay

### Step 5: Create Routes
Cần tạo 3 route files:
- `BudgetRoutes.kt`
- `RecurringTransactionRoutes.kt`
- `DebtRoutes.kt`

### Step 6: Add Scheduler
Implement scheduler để tự động tạo recurring transactions:
- Chạy mỗi ngày lúc 00:00
- Check các recurring transactions đến hạn
- Tự động tạo transaction nếu `auto_create = true`

---

## 🎯 Tính năng chính

### 1. BUDGET MANAGEMENT (Quản lý Ngân sách)

#### Features:
- ✅ Đặt ngân sách cho từng category
- ✅ Hỗ trợ nhiều period: daily, weekly, monthly, yearly
- ✅ Theo dõi chi tiêu real-time
- ✅ Alert khi vượt threshold (default 80%)
- ✅ Dashboard tổng hợp

#### API Endpoints:
```
POST   /api/v1/budgets              # Tạo ngân sách mới
GET    /api/v1/budgets              # List ngân sách
GET    /api/v1/budgets/{id}         # Chi tiết ngân sách
PUT    /api/v1/budgets/{id}         # Cập nhật ngân sách
DELETE /api/v1/budgets/{id}         # Xóa ngân sách
GET    /api/v1/budgets/spending     # Chi tiêu theo ngân sách
GET    /api/v1/budgets/summary      # Tổng hợp ngân sách
```

#### Example Request:
```json
POST /api/v1/budgets
{
  "categoryId": "uuid",
  "amount": "5000000",
  "period": "monthly",
  "startDate": "2025-01-01",
  "alertPercentage": "80"
}
```

#### Example Response:
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "categoryName": "Ăn uống",
    "budgetAmount": "5000000",
    "spentAmount": "3500000",
    "remainingAmount": "1500000",
    "spentPercentage": "70.00",
    "isExceeded": false,
    "shouldAlert": false
  }
}
```

---

### 2. RECURRING TRANSACTIONS (Giao dịch Lặp lại)

#### Features:
- ✅ Tạo giao dịch lặp lại tự động
- ✅ Hỗ trợ: daily, weekly, monthly, yearly
- ✅ Tùy chỉnh ngày cụ thể (day of month/week)
- ✅ Tự động tạo transaction hoặc notification
- ✅ Quản lý active/inactive

#### API Endpoints:
```
POST   /api/v1/recurring-transactions              # Tạo recurring
GET    /api/v1/recurring-transactions              # List recurring
GET    /api/v1/recurring-transactions/{id}         # Chi tiết
PUT    /api/v1/recurring-transactions/{id}         # Cập nhật
DELETE /api/v1/recurring-transactions/{id}         # Xóa
GET    /api/v1/recurring-transactions/due          # Các transaction sắp đến
POST   /api/v1/recurring-transactions/{id}/execute # Thực thi manual
GET    /api/v1/recurring-transactions/summary      # Tổng hợp
```

#### Example Request:
```json
POST /api/v1/recurring-transactions
{
  "categoryId": "uuid",
  "description": "Tiền nhà",
  "amount": "3000000",
  "frequency": "monthly",
  "startDate": "2025-01-01",
  "autoCreate": true,
  "dayOfMonth": 1
}
```

#### Example Response:
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "categoryName": "Chi phí cố định",
    "description": "Tiền nhà",
    "amount": "3000000",
    "frequency": "monthly",
    "nextOccurrence": "2025-12-01",
    "isActive": true,
    "autoCreate": true
  }
}
```

---

### 3. DEBT & LOAN MANAGEMENT (Quản lý Nợ/Cho vay)

#### Features:
- ✅ Quản lý khoản vay (borrowed) và cho vay (lent)
- ✅ Theo dõi lãi suất
- ✅ Lịch sử thanh toán
- ✅ Tự động cập nhật status (active, partial, paid, overdue)
- ✅ Thống kê tổng nợ/cho vay

#### API Endpoints:
```
# Debt Management
POST   /api/v1/debts              # Tạo debt mới
GET    /api/v1/debts              # List debts
GET    /api/v1/debts/{id}         # Chi tiết debt
PUT    /api/v1/debts/{id}         # Cập nhật debt
DELETE /api/v1/debts/{id}         # Xóa debt
GET    /api/v1/debts/summary      # Tổng hợp nợ
GET    /api/v1/debts/overdue      # Các khoản quá hạn

# Debt Payments
POST   /api/v1/debts/{id}/payments         # Thêm payment
GET    /api/v1/debts/{id}/payments         # Lịch sử payment
DELETE /api/v1/debts/payments/{paymentId}  # Xóa payment
```

#### Example Request (Create Debt):
```json
POST /api/v1/debts
{
  "type": "borrowed",
  "personName": "Nguyễn Văn A",
  "personContact": "0912345678",
  "amount": "10000000",
  "interestRate": "5.0",
  "description": "Vay tiền mua xe",
  "dueDate": "2025-12-31",
  "startDate": "2025-01-01"
}
```

#### Example Request (Add Payment):
```json
POST /api/v1/debts/{debtId}/payments
{
  "amount": "2000000",
  "paymentDate": "2025-11-16",
  "notes": "Trả nợ đợt 1"
}
```

#### Example Response (Debt Summary):
```json
{
  "success": true,
  "data": {
    "totalBorrowed": "10000000",
    "totalLent": "5000000",
    "totalBorrowedRemaining": "8000000",
    "totalLentRemaining": "3000000",
    "totalOverdue": 2,
    "borrowedDebts": [...],
    "lentDebts": [...]
  }
}
```

---

## 🔄 Recurring Transaction Scheduler

### Implementation với Kotlin Coroutines:

```kotlin
// In Application.kt
launch {
    while (true) {
        delay(Duration.ofHours(1)) // Check mỗi giờ
        recurringTransactionService.processRecurringTransactions()
    }
}
```

### Logic:
1. Tìm các recurring transactions có `next_occurrence <= today`
2. Nếu `auto_create = true`:
   - Tạo transaction mới
   - Cập nhật `next_occurrence`
3. Nếu `auto_create = false`:
   - Gửi notification
4. Check `end_date` và deactivate nếu hết hạn

---

## 📊 Database Views Usage

### Budget Spending View:
```sql
SELECT * FROM budget_spending_summary 
WHERE user_id = 'uuid' 
ORDER BY spent_percentage DESC;
```

### Active Debts View:
```sql
SELECT * FROM active_debts_summary 
WHERE user_id = 'uuid' 
  AND status != 'paid'
ORDER BY days_overdue DESC;
```

---

## 🎨 UI/UX Suggestions

### Budget Screen:
- Progress bars cho mỗi category
- Color coding: Green (< 70%), Yellow (70-90%), Red (> 90%)
- Push notification khi vượt threshold

### Recurring Transactions Screen:
- Calendar view
- Toggle active/inactive
- "Execute now" button

### Debt Screen:
- Tabs: "I Owe" vs "They Owe Me"
- Payment history timeline
- Interest calculator

---

## 🔐 Security & Validation

### Budget:
- ✅ Validate amount > 0
- ✅ Validate period in allowed values
- ✅ Validate alert_percentage 0-100
- ✅ Check user owns category

### Recurring:
- ✅ Validate frequency
- ✅ Validate day_of_month (1-31)
- ✅ Validate day_of_week (0-6)
- ✅ end_date >= start_date

### Debt:
- ✅ Validate type ('borrowed'/'lent')
- ✅ Validate payment amount <= remaining_amount
- ✅ Check user owns debt

---

## 🧪 Testing Checklist

### Budget:
- [ ] Create budget with different periods
- [ ] Update budget amount
- [ ] Check spending calculation
- [ ] Test alert trigger
- [ ] Delete budget

### Recurring:
- [ ] Create monthly recurring
- [ ] Create weekly recurring
- [ ] Test auto-create
- [ ] Test next occurrence calculation
- [ ] Deactivate/reactivate

### Debt:
- [ ] Create borrowed debt
- [ ] Create lent debt
- [ ] Add payment
- [ ] Check remaining amount update
- [ ] Check status auto-update
- [ ] Test overdue calculation

---

## 📝 Next Steps

1. **Implement Repositories** (3 files)
2. **Implement Services** (3 files)
3. **Create Routes** (3 files)
4. **Add Scheduler** (1 file)
5. **Update Application.kt** (DI setup)
6. **Test all endpoints**
7. **Add validation middleware**
8. **Create frontend integration docs**

---

## 🚀 Quick Implementation Command

Sau khi tạo xong repositories, services, routes:

```bash
# Run migration
psql -U root -d financial_db_dev -f migration_budget_recurring_debt.sql

# Build
.\gradlew.bat build -x test

# Run
.\gradlew.bat run
```

---

## 📚 Documentation Files to Create

- `BUDGET_API_GUIDE.md` - Budget API documentation
- `RECURRING_API_GUIDE.md` - Recurring transaction docs
- `DEBT_API_GUIDE.md` - Debt management docs
- `SCHEDULER_GUIDE.md` - Recurring scheduler setup

---

**Status:** 📋 Phase 1 Complete - Ready for Implementation

**Next:** Implement Repositories → Services → Routes → Scheduler

