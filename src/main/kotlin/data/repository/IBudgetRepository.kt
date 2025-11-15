package com.financial.data.repository

import com.financial.data.model.Budget
import com.financial.data.model.BudgetSpending
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

interface IBudgetRepository {
    suspend fun findById(id: UUID): Budget?
    suspend fun findByUserId(userId: UUID): List<Budget>
    suspend fun findByUserIdAndPeriod(userId: UUID, period: String): List<Budget>
    suspend fun findActiveByUserId(userId: UUID): List<Budget>
    suspend fun getBudgetSpending(userId: UUID): List<BudgetSpending>
    suspend fun getBudgetSpendingByPeriod(userId: UUID, period: String): List<BudgetSpending>
    suspend fun create(
        userId: UUID,
        categoryId: UUID,
        amount: BigDecimal,
        period: String,
        startDate: LocalDate,
        endDate: LocalDate?,
        alertPercentage: BigDecimal
    ): Budget
    suspend fun update(
        id: UUID,
        amount: BigDecimal?,
        period: String?,
        endDate: LocalDate?,
        isActive: Boolean?,
        alertPercentage: BigDecimal?
    ): Budget?
    suspend fun delete(id: UUID): Boolean
}

