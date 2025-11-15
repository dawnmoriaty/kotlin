package com.financial.data.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.Instant
import java.util.*

data class Budget(
    val id: UUID,
    val userId: UUID,
    val categoryId: UUID,
    val amount: BigDecimal,
    val period: String, // 'daily', 'weekly', 'monthly', 'yearly'
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val isActive: Boolean,
    val alertPercentage: BigDecimal,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class BudgetSpending(
    val budget: Budget,
    val categoryName: String,
    val spentAmount: BigDecimal,
    val remainingAmount: BigDecimal,
    val spentPercentage: BigDecimal,
    val isExceeded: Boolean,
    val shouldAlert: Boolean
)

data class RecurringTransaction(
    val id: UUID,
    val userId: UUID,
    val categoryId: UUID,
    val description: String,
    val amount: BigDecimal,
    val frequency: String, // 'daily', 'weekly', 'monthly', 'yearly'
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val nextOccurrence: LocalDate,
    val isActive: Boolean,
    val autoCreate: Boolean,
    val dayOfMonth: Int?,
    val dayOfWeek: Int?,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class Debt(
    val id: UUID,
    val userId: UUID,
    val type: String, // 'borrowed', 'lent'
    val personName: String,
    val personContact: String?,
    val amount: BigDecimal,
    val remainingAmount: BigDecimal,
    val interestRate: BigDecimal,
    val description: String?,
    val dueDate: LocalDate?,
    val status: String, // 'active', 'partial', 'paid', 'overdue'
    val startDate: LocalDate,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class DebtPayment(
    val id: UUID,
    val debtId: UUID,
    val amount: BigDecimal,
    val paymentDate: LocalDate,
    val notes: String?,
    val createdAt: Instant
)

data class DebtSummary(
    val debt: Debt,
    val paidAmount: BigDecimal,
    val paidPercentage: BigDecimal,
    val paymentCount: Int,
    val lastPaymentDate: LocalDate?,
    val daysOverdue: Int
)

