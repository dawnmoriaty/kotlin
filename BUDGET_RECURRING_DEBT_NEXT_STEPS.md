# 🎯 Budget, Recurring & Debt - Services Implementation

## Files đã tạo:
- ✅ BudgetRepository.kt
- ✅ RecurringTransactionRepository.kt  
- ✅ DebtRepository.kt & DebtPaymentRepository.kt

## Tiếp theo cần tạo:

### 1. Services (trong `domain/services/impl/`)

#### BudgetService.kt
- getBudgets(userId)
- getBudgetById(userId, budgetId)
- getBudgetSpending(userId)
- getBudgetSummary(userId)
- createBudget(userId, request)
- updateBudget(userId, budgetId, request)
- deleteBudget(userId, budgetId)

#### RecurringTransactionService.kt
- getRecurringTransactions(userId)
- getRecurringTransactionById(userId, id)
- getDueRecurringTransactions(userId)
- getRecurringSummary(userId)
- createRecurringTransaction(userId, request)
- updateRecurringTransaction(userId, id, request)
- deleteRecurringTransaction(userId, id)
- executeRecurringTransaction(userId, id) // Manual execution
- **processRecurringTransactions()** // Scheduler method

#### DebtService.kt
- getDebts(userId, type?)
- getDebtById(userId, debtId)
- getDebtDetail(userId, debtId)
- getDebtSummary(userId)
- getOverdueDebts(userId)
- createDebt(userId, request)
- updateDebt(userId, debtId, request)
- deleteDebt(userId, debtId)
- addPayment(userId, debtId, request)
- getPayments(userId, debtId)
- deletePayment(userId, debtId, paymentId)

### 2. Routes (trong `routes/`)

#### BudgetRoutes.kt
```kotlin
GET    /api/v1/budgets
GET    /api/v1/budgets/:id
GET    /api/v1/budgets/spending
GET    /api/v1/budgets/summary
POST   /api/v1/budgets
PUT    /api/v1/budgets/:id
DELETE /api/v1/budgets/:id
```

#### RecurringTransactionRoutes.kt
```kotlin
GET    /api/v1/recurring-transactions
GET    /api/v1/recurring-transactions/:id
GET    /api/v1/recurring-transactions/due
GET    /api/v1/recurring-transactions/summary
POST   /api/v1/recurring-transactions
PUT    /api/v1/recurring-transactions/:id
DELETE /api/v1/recurring-transactions/:id
POST   /api/v1/recurring-transactions/:id/execute
```

#### DebtRoutes.kt
```kotlin
GET    /api/v1/debts
GET    /api/v1/debts/:id
GET    /api/v1/debts/:id/detail
GET    /api/v1/debts/summary
GET    /api/v1/debts/overdue
POST   /api/v1/debts
PUT    /api/v1/debts/:id
DELETE /api/v1/debts/:id
POST   /api/v1/debts/:id/payments
GET    /api/v1/debts/:id/payments
DELETE /api/v1/debts/payments/:paymentId
```

### 3. Enhanced Dashboard API

#### DashboardService.kt - Enhanced
Thêm vào TransactionService hoặc tạo DashboardService riêng:

```kotlin
data class FinancialDashboard(
    // Existing
    val totalIncome: BigDecimal,
    val totalExpense: BigDecimal,
    val balance: BigDecimal,
    
    // Budget info
    val budgetSummary: BudgetDashboard,
    
    // Recurring info
    val upcomingRecurring: RecurringDashboard,
    
    // Debt info
    val debtSummary: DebtDashboard,
    
    // Trends
    val monthlyTrend: List<MonthlyTrend>,
    val categoryBreakdown: List<CategorySpending>
)

data class BudgetDashboard(
    val totalBudget: BigDecimal,
    val totalSpent: BigDecimal,
    val remainingBudget: BigDecimal,
    val exceededCount: Int,
    val alertCount: Int,
    val topExceededCategories: List<BudgetAlert>
)

data class RecurringDashboard(
    val totalMonthlyRecurring: BigDecimal,
    val upcomingCount: Int,
    val nextDueDate: LocalDate?,
    val upcomingTransactions: List<RecurringPreview>
)

data class DebtDashboard(
    val totalBorrowed: BigDecimal,
    val totalLent: BigDecimal,
    val borrowedRemaining: BigDecimal,
    val lentRemaining: BigDecimal,
    val overdueCount: Int,
    val upcomingDueDate: LocalDate?
)
```

#### Enhanced Dashboard Endpoint:
```kotlin
GET /api/v1/dashboard/complete
```

Response:
```json
{
  "success": true,
  "data": {
    "overview": {
      "totalIncome": "50000000",
      "totalExpense": "35000000",
      "balance": "15000000",
      "savingsRate": "30.00"
    },
    "budget": {
      "totalBudget": "40000000",
      "totalSpent": "35000000",
      "remainingBudget": "5000000",
      "spentPercentage": "87.50",
      "exceededCount": 2,
      "alertCount": 5,
      "topExceeded": [...]
    },
    "recurring": {
      "totalMonthlyRecurring": "8000000",
      "totalYearlyRecurring": "96000000",
      "upcomingCount": 5,
      "nextDueDate": "2025-12-01",
      "upcoming": [...]
    },
    "debt": {
      "totalBorrowed": "10000000",
      "totalLent": "5000000",
      "borrowedRemaining": "7000000",
      "lentRemaining": "3000000",
      "overdueCount": 1,
      "netDebt": "2000000"
    },
    "trends": {
      "last6Months": [...],
      "categoryBreakdown": [...]
    }
  }
}
```

### 4. Scheduler Setup

#### Application.kt - Add Scheduler
```kotlin
// In module() function
GlobalScope.launch {
    val recurringService = RecurringTransactionService(...)
    
    while (true) {
        try {
            recurringService.processRecurringTransactions()
        } catch (e: Exception) {
            logger.error("Error processing recurring transactions", e)
        }
        
        // Run every hour
        delay(Duration.ofHours(1).toMillis())
    }
}
```

### 5. Update DatabaseFactory.kt

```kotlin
SchemaUtils.create(
    Users, 
    Profiles, 
    Categories, 
    Transactions, 
    RefreshTokens, 
    PasswordResetTokens,
    Budgets,                    // ADD
    RecurringTransactions,      // ADD
    Debts,                      // ADD
    DebtPayments                // ADD
)
```

### 6. Update Routing.kt

```kotlin
fun Application.configureRouting(
    authService: IAuthService,
    userService: IUserService,
    categoryService: ICategoryService,
    transactionService: ITransactionService,
    budgetService: IBudgetService,              // ADD
    recurringService: IRecurringTransactionService,  // ADD
    debtService: IDebtService                   // ADD
) {
    routing {
        authRoutes(authService)
        
        authenticate("auth-jwt") {
            userRoutes(userService)
            categoryRoutes(categoryService)
            transactionRoutes(transactionService)
            budgetRoutes(budgetService)              // ADD
            recurringTransactionRoutes(recurringService)  // ADD
            debtRoutes(debtService)                  // ADD
        }
    }
}
```

---

## Quick Commands

### 1. Run Migration
```bash
psql -U root -d financial_db_dev -f migration_budget_recurring_debt.sql
```

### 2. Verify Tables
```sql
\dt budgets
\dt recurring_transactions
\dt debts
\dt debt_payments
```

### 3. Test Budget
```bash
curl -X POST http://localhost:8080/api/v1/budgets \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": "uuid",
    "amount": "5000000",
    "period": "monthly",
    "startDate": "2025-11-01",
    "alertPercentage": "80"
  }'
```

### 4. Test Recurring
```bash
curl -X POST http://localhost:8080/api/v1/recurring-transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": "uuid",
    "description": "Tiền nhà",
    "amount": "3000000",
    "frequency": "monthly",
    "startDate": "2025-11-01",
    "autoCreate": true,
    "dayOfMonth": 1
  }'
```

### 5. Test Debt
```bash
curl -X POST http://localhost:8080/api/v1/debts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "borrowed",
    "personName": "Nguyễn Văn A",
    "personContact": "0912345678",
    "amount": "10000000",
    "interestRate": "5.0",
    "dueDate": "2025-12-31"
  }'
```

### 6. Test Enhanced Dashboard
```bash
curl -X GET http://localhost:8080/api/v1/dashboard/complete \
  -H "Authorization: Bearer $TOKEN"
```

---

## Implementation Priority

1. ✅ **Repositories** (DONE)
2. **Services** (Next - 3 files)
3. **Routes** (3 files)
4. **Enhanced Dashboard** (1 file)
5. **Scheduler** (Update Application.kt)
6. **Testing** (Postman collection)

---

## Status: Phase 2 - Ready for Services Implementation

Repositories are complete. Next: Implement Services!

