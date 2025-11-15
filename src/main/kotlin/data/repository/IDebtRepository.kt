package com.financial.data.repository

import com.financial.data.model.Debt
import com.financial.data.model.DebtPayment
import com.financial.data.model.DebtSummary
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

interface IDebtRepository {
    suspend fun findById(id: UUID): Debt?
    suspend fun findByUserId(userId: UUID): List<Debt>
    suspend fun findByUserIdAndType(userId: UUID, type: String): List<Debt>
    suspend fun findActiveByUserId(userId: UUID): List<Debt>
    suspend fun findOverdueByUserId(userId: UUID): List<Debt>
    suspend fun getDebtSummary(debtId: UUID): DebtSummary?
    suspend fun getDebtSummaries(userId: UUID): List<DebtSummary>
    suspend fun create(
        userId: UUID,
        type: String,
        personName: String,
        personContact: String?,
        amount: BigDecimal,
        interestRate: BigDecimal,
        description: String?,
        dueDate: LocalDate?,
        startDate: LocalDate
    ): Debt
    suspend fun update(
        id: UUID,
        personName: String?,
        personContact: String?,
        interestRate: BigDecimal?,
        description: String?,
        dueDate: LocalDate?,
        status: String?
    ): Debt?
    suspend fun delete(id: UUID): Boolean
}

interface IDebtPaymentRepository {
    suspend fun findById(id: UUID): DebtPayment?
    suspend fun findByDebtId(debtId: UUID): List<DebtPayment>
    suspend fun create(
        debtId: UUID,
        amount: BigDecimal,
        paymentDate: LocalDate,
        notes: String?
    ): DebtPayment
    suspend fun delete(id: UUID): Boolean
}

