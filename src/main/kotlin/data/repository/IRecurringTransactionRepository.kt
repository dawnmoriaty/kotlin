package com.financial.data.repository

import com.financial.data.model.RecurringTransaction
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

interface IRecurringTransactionRepository {
    suspend fun findById(id: UUID): RecurringTransaction?
    suspend fun findByUserId(userId: UUID): List<RecurringTransaction>
    suspend fun findActiveByUserId(userId: UUID): List<RecurringTransaction>
    suspend fun findDueTransactions(currentDate: LocalDate): List<RecurringTransaction>
    suspend fun findDueByUserId(userId: UUID, currentDate: LocalDate): List<RecurringTransaction>
    suspend fun create(
        userId: UUID,
        categoryId: UUID,
        description: String,
        amount: BigDecimal,
        frequency: String,
        startDate: LocalDate,
        endDate: LocalDate?,
        autoCreate: Boolean,
        dayOfMonth: Int?,
        dayOfWeek: Int?
    ): RecurringTransaction
    suspend fun update(
        id: UUID,
        description: String?,
        amount: BigDecimal?,
        frequency: String?,
        endDate: LocalDate?,
        isActive: Boolean?,
        autoCreate: Boolean?,
        dayOfMonth: Int?,
        dayOfWeek: Int?
    ): RecurringTransaction?
    suspend fun updateNextOccurrence(id: UUID, nextOccurrence: LocalDate): Boolean
    suspend fun delete(id: UUID): Boolean
}

