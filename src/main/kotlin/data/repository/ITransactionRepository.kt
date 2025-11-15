package com.financial.data.repository

import com.financial.data.model.Transaction
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

interface ITransactionRepository {
    suspend fun findById(id: UUID): Transaction?
    suspend fun findByUserId(
        userId: UUID,
        categoryId: UUID? = null,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        limit: Int = 50,
        offset: Int = 0
    ): List<Transaction>

    suspend fun create(
        description: String,
        amount: BigDecimal,
        transactionDate: LocalDate,
        categoryId: UUID,
        userId: UUID
    ): Transaction

    suspend fun update(transaction: Transaction): Transaction
    suspend fun delete(id: UUID, userId: UUID): Boolean
    suspend fun countByUserId(userId: UUID): Long

    // Statistics
    suspend fun getTotalIncomeByUserId(userId: UUID, startDate: LocalDate?, endDate: LocalDate?): BigDecimal
    suspend fun getTotalExpenseByUserId(userId: UUID, startDate: LocalDate?, endDate: LocalDate?): BigDecimal
    suspend fun getTransactionCountByType(userId: UUID, type: String, startDate: LocalDate?, endDate: LocalDate?): Long
}

