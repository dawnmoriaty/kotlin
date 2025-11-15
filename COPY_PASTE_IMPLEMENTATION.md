# 📦 Complete Implementation Code - Copy & Paste Ready

## ✅ Phase 1 Complete: Repositories
- BudgetRepository.kt ✅
- RecurringTransactionRepository.kt ✅
- DebtRepository.kt ✅

## 🔨 Phase 2: Services - Copy these files

### File 1: `src/main/kotlin/domain/services/impl/BudgetService.kt`

```kotlin
package com.financial.domain.service.impl

import com.financial.data.repository.IBudgetRepository
import com.financial.data.repository.ICategoryRepository
import com.financial.domain.exceptions.NotFoundException
import com.financial.domain.services.IBudgetService
import com.financial.dtos.request.CreateBudgetRequest
import com.financial.dtos.request.UpdateBudgetRequest
import com.financial.dtos.response.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

class BudgetService(
    private val budgetRepository: IBudgetRepository,
    private val categoryRepository: ICategoryRepository
) : IBudgetService {

    override suspend fun getBudgets(userId: UUID): List<BudgetResponse> {
        val budgets = budgetRepository.findByUserId(userId)
        return budgets.map { budget ->
            val category = categoryRepository.findById(budget.categoryId)
            BudgetResponse(
                id = budget.id.toString(),
                categoryId = budget.categoryId.toString(),
                categoryName = category?.name ?: "Unknown",
                amount = budget.amount.toString(),
                period = budget.period,
                startDate = budget.startDate.toString(),
                endDate = budget.endDate?.toString(),
                isActive = budget.isActive,
                alertPercentage = budget.alertPercentage.toString(),
                createdAt = budget.createdAt.toString(),
                updatedAt = budget.updatedAt.toString()
            )
        }
    }

    override suspend fun getBudgetById(userId: UUID, budgetId: UUID): BudgetResponse? {
        val budget = budgetRepository.findById(budgetId) ?: return null
        if (budget.userId != userId) return null

        val category = categoryRepository.findById(budget.categoryId)
        return BudgetResponse(
            id = budget.id.toString(),
            categoryId = budget.categoryId.toString(),
            categoryName = category?.name ?: "Unknown",
            amount = budget.amount.toString(),
            period = budget.period,
            startDate = budget.startDate.toString(),
            endDate = budget.endDate?.toString(),
            isActive = budget.isActive,
            alertPercentage = budget.alertPercentage.toString(),
            createdAt = budget.createdAt.toString(),
            updatedAt = budget.updatedAt.toString()
        )
    }

    override suspend fun getBudgetSpending(userId: UUID, period: String?): List<BudgetSpendingResponse> {
        val budgetSpending = if (period != null) {
            budgetRepository.getBudgetSpendingByPeriod(userId, period)
        } else {
            budgetRepository.getBudgetSpending(userId)
        }

        return budgetSpending.map {
            BudgetSpendingResponse(
                id = it.budget.id.toString(),
                categoryId = it.budget.categoryId.toString(),
                categoryName = it.categoryName,
                budgetAmount = it.budget.amount.toString(),
                spentAmount = it.spentAmount.toString(),
                remainingAmount = it.remainingAmount.toString(),
                spentPercentage = it.spentPercentage.toString(),
                period = it.budget.period,
                isExceeded = it.isExceeded,
                shouldAlert = it.shouldAlert,
                startDate = it.budget.startDate.toString(),
                endDate = it.budget.endDate?.toString()
            )
        }
    }

    override suspend fun getBudgetSummary(userId: UUID): BudgetSummaryResponse {
        val budgetSpending = budgetRepository.getBudgetSpending(userId)

        val totalBudget = budgetSpending.sumOf { it.budget.amount }
        val totalSpent = budgetSpending.sumOf { it.spentAmount }
        val totalRemaining = totalBudget - totalSpent
        val overallPercentage = if (totalBudget > BigDecimal.ZERO) {
            (totalSpent / totalBudget * BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP)
        } else BigDecimal.ZERO

        return BudgetSummaryResponse(
            totalBudget = totalBudget.toString(),
            totalSpent = totalSpent.toString(),
            totalRemaining = totalRemaining.toString(),
            overallSpentPercentage = overallPercentage.toString(),
            exceededCount = budgetSpending.count { it.isExceeded },
            alertCount = budgetSpending.count { it.shouldAlert },
            budgets = getBudgetSpending(userId, null)
        )
    }

    override suspend fun createBudget(userId: UUID, request: CreateBudgetRequest): BudgetResponse {
        // Validate category ownership
        val category = categoryRepository.findById(UUID.fromString(request.categoryId))
            ?: throw NotFoundException("Category not found")

        if (category.userId != userId) {
            throw NotFoundException("Category not found")
        }

        // Validate period
        val validPeriods = listOf("daily", "weekly", "monthly", "yearly")
        if (request.period !in validPeriods) {
            throw IllegalArgumentException("Invalid period. Must be one of: ${validPeriods.joinToString()}")
        }

        val budget = budgetRepository.create(
            userId = userId,
            categoryId = UUID.fromString(request.categoryId),
            amount = BigDecimal(request.amount),
            period = request.period,
            startDate = LocalDate.parse(request.startDate),
            endDate = request.endDate?.let { LocalDate.parse(it) },
            alertPercentage = BigDecimal(request.alertPercentage ?: "80.00")
        )

        return BudgetResponse(
            id = budget.id.toString(),
            categoryId = budget.categoryId.toString(),
            categoryName = category.name,
            amount = budget.amount.toString(),
            period = budget.period,
            startDate = budget.startDate.toString(),
            endDate = budget.endDate?.toString(),
            isActive = budget.isActive,
            alertPercentage = budget.alertPercentage.toString(),
            createdAt = budget.createdAt.toString(),
            updatedAt = budget.updatedAt.toString()
        )
    }

    override suspend fun updateBudget(userId: UUID, budgetId: UUID, request: UpdateBudgetRequest): BudgetResponse? {
        val existing = budgetRepository.findById(budgetId) ?: return null
        if (existing.userId != userId) return null

        val updated = budgetRepository.update(
            id = budgetId,
            amount = request.amount?.let { BigDecimal(it) },
            period = request.period,
            endDate = request.endDate?.let { LocalDate.parse(it) },
            isActive = request.isActive,
            alertPercentage = request.alertPercentage?.let { BigDecimal(it) }
        ) ?: return null

        val category = categoryRepository.findById(updated.categoryId)
        return BudgetResponse(
            id = updated.id.toString(),
            categoryId = updated.categoryId.toString(),
            categoryName = category?.name ?: "Unknown",
            amount = updated.amount.toString(),
            period = updated.period,
            startDate = updated.startDate.toString(),
            endDate = updated.endDate?.toString(),
            isActive = updated.isActive,
            alertPercentage = updated.alertPercentage.toString(),
            createdAt = updated.createdAt.toString(),
            updatedAt = updated.updatedAt.toString()
        )
    }

    override suspend fun deleteBudget(userId: UUID, budgetId: UUID): Boolean {
        val budget = budgetRepository.findById(budgetId) ?: return false
        if (budget.userId != userId) return false
        return budgetRepository.delete(budgetId)
    }
}
```

---

### File 2: `src/main/kotlin/routes/BudgetRoutes.kt`

```kotlin
package com.financial.routes

import com.financial.domain.services.IBudgetService
import com.financial.dtos.request.CreateBudgetRequest
import com.financial.dtos.request.UpdateBudgetRequest
import com.financial.dtos.response.ApiResponse
import com.financial.plugins.getUserId
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.budgetRoutes(budgetService: IBudgetService) {
    route("/api/v1/budgets") {
        
        // Get all budgets
        get {
            val userId = call.getUserId()
            val budgets = budgetService.getBudgets(userId)
            call.respond(HttpStatusCode.OK, ApiResponse.success(budgets))
        }

        // Get budget spending
        get("/spending") {
            val userId = call.getUserId()
            val period = call.request.queryParameters["period"]
            val spending = budgetService.getBudgetSpending(userId, period)
            call.respond(HttpStatusCode.OK, ApiResponse.success(spending))
        }

        // Get budget summary
        get("/summary") {
            val userId = call.getUserId()
            val summary = budgetService.getBudgetSummary(userId)
            call.respond(HttpStatusCode.OK, ApiResponse.success(summary))
        }

        // Get budget by ID
        get("/{id}") {
            val userId = call.getUserId()
            val budgetId = UUID.fromString(call.parameters["id"]!!)
            val budget = budgetService.getBudgetById(userId, budgetId)
            
            if (budget == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse.error<Unit>("Budget not found"))
            } else {
                call.respond(HttpStatusCode.OK, ApiResponse.success(budget))
            }
        }

        // Create budget
        post {
            val userId = call.getUserId()
            val request = call.receive<CreateBudgetRequest>()
            val budget = budgetService.createBudget(userId, request)
            call.respond(HttpStatusCode.Created, ApiResponse.success(budget, "Budget created successfully"))
        }

        // Update budget
        put("/{id}") {
            val userId = call.getUserId()
            val budgetId = UUID.fromString(call.parameters["id"]!!)
            val request = call.receive<UpdateBudgetRequest>()
            val budget = budgetService.updateBudget(userId, budgetId, request)
            
            if (budget == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse.error<Unit>("Budget not found"))
            } else {
                call.respond(HttpStatusCode.OK, ApiResponse.success(budget, "Budget updated successfully"))
            }
        }

        // Delete budget
        delete("/{id}") {
            val userId = call.getUserId()
            val budgetId = UUID.fromString(call.parameters["id"]!!)
            val deleted = budgetService.deleteBudget(userId, budgetId)
            
            if (deleted) {
                call.respond(HttpStatusCode.OK, ApiResponse.success<Unit>(message = "Budget deleted successfully"))
            } else {
                call.respond(HttpStatusCode.NotFound, ApiResponse.error<Unit>("Budget not found"))
            }
        }
    }
}
```

---

## 🚀 Quick Setup Steps

### 1. Copy Repositories (Already Created)
- ✅ BudgetRepository.kt
- ✅ RecurringTransactionRepository.kt
- ✅ DebtRepository.kt

### 2. Copy Services
- Copy BudgetService.kt above
- Create similar for RecurringTransactionService.kt
- Create similar for DebtService.kt

### 3. Copy Routes
- Copy BudgetRoutes.kt above
- Create similar for RecurringTransactionRoutes.kt
- Create similar for DebtRoutes.kt

### 4. Update DatabaseFactory.kt
```kotlin
SchemaUtils.create(
    Users, Profiles, Categories, Transactions, RefreshTokens, PasswordResetTokens,
    Budgets, RecurringTransactions, Debts, DebtPayments  // ADD THESE
)
```

### 5. Update Application.kt
```kotlin
val budgetService = BudgetService(
    budgetRepository = BudgetRepository(),
    categoryRepository = CategoryRepository()
)

// Pass to configureRouting
configureRouting(authService, userService, categoryService, transactionService, budgetService)
```

### 6. Update Routing.kt
```kotlin
authenticate("auth-jwt") {
    userRoutes(userService)
    categoryRoutes(categoryService)
    transactionRoutes(transactionService)
    budgetRoutes(budgetService)  // ADD THIS
}
```

### 7. Run Migration
```bash
psql -U root -d financial_db_dev -f migration_budget_recurring_debt.sql
```

### 8. Build & Run
```bash
.\gradlew.bat build -x test
.\gradlew.bat run
```

---

## 📝 Next Files to Create

Do bạn muốn tôi tạo đầy đủ, tôi sẽ list ra files còn lại cần:

1. ✅ **Repositories** (DONE)
   - BudgetRepository.kt
   - RecurringTransactionRepository.kt
   - DebtRepository.kt

2. **Services** (1/3 done)
   - ✅ BudgetService.kt (code above)
   - ⏳ RecurringTransactionService.kt
   - ⏳ DebtService.kt

3. **Routes** (1/3 done)
   - ✅ BudgetRoutes.kt (code above)
   - ⏳ RecurringTransactionRoutes.kt
   - ⏳ DebtRoutes.kt

4. **Enhanced Dashboard**
   - ⏳ Update TransactionService with complete dashboard

---

## ✅ What You Can Test Now

With Budget feature:
```bash
# Create budget
curl -X POST http://localhost:8080/api/v1/budgets \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": "uuid",
    "amount": "5000000",
    "period": "monthly",
    "startDate": "2025-11-01"
  }'

# Get spending
curl -X GET http://localhost:8080/api/v1/budgets/spending \
  -H "Authorization: Bearer $TOKEN"

# Get summary
curl -X GET http://localhost:8080/api/v1/budgets/summary \
  -H "Authorization: Bearer $TOKEN"
```

---

**Bạn có muốn tôi tiếp tục tạo RecurringTransactionService và DebtService không?** 

Hoặc bạn muốn tôi tạo Enhanced Dashboard trước? Tôi đang ưu tiên hoàn thiện từng phần để bạn có thể test ngay! 🚀

