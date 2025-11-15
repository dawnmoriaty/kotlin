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

