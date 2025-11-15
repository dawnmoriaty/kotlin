package com.financial.domain.services

import com.financial.dtos.request.CreateBudgetRequest
import com.financial.dtos.request.UpdateBudgetRequest
import com.financial.dtos.response.BudgetResponse
import com.financial.dtos.response.BudgetSpendingResponse
import com.financial.dtos.response.BudgetSummaryResponse
import java.util.*

interface IBudgetService {
    suspend fun getBudgets(userId: UUID): List<BudgetResponse>
    suspend fun getBudgetById(userId: UUID, budgetId: UUID): BudgetResponse?
    suspend fun getBudgetSpending(userId: UUID, period: String?): List<BudgetSpendingResponse>
    suspend fun getBudgetSummary(userId: UUID): BudgetSummaryResponse
    suspend fun createBudget(userId: UUID, request: CreateBudgetRequest): BudgetResponse
    suspend fun updateBudget(userId: UUID, budgetId: UUID, request: UpdateBudgetRequest): BudgetResponse?
    suspend fun deleteBudget(userId: UUID, budgetId: UUID): Boolean
}

