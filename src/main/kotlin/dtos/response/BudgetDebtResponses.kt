package com.financial.dtos.response

import kotlinx.serialization.Serializable

// ==================== BUDGET RESPONSES ====================

@Serializable
data class BudgetResponse(
    val id: String,
    val categoryId: String,
    val categoryName: String,
    val amount: String,
    val period: String,
    val startDate: String,
    val endDate: String?,
    val isActive: Boolean,
    val alertPercentage: String,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class BudgetSpendingResponse(
    val id: String,
    val categoryId: String,
    val categoryName: String,
    val budgetAmount: String,
    val spentAmount: String,
    val remainingAmount: String,
    val spentPercentage: String,
    val period: String,
    val isExceeded: Boolean,
    val shouldAlert: Boolean,
    val startDate: String,
    val endDate: String?
)

@Serializable
data class BudgetSummaryResponse(
    val totalBudget: String,
    val totalSpent: String,
    val totalRemaining: String,
    val overallSpentPercentage: String,
    val exceededCount: Int,
    val alertCount: Int,
    val budgets: List<BudgetSpendingResponse>
)

// ==================== RECURRING TRANSACTION RESPONSES ====================

@Serializable
data class RecurringTransactionResponse(
    val id: String,
    val categoryId: String,
    val categoryName: String,
    val description: String,
    val amount: String,
    val frequency: String,
    val startDate: String,
    val endDate: String?,
    val nextOccurrence: String,
    val isActive: Boolean,
    val autoCreate: Boolean,
    val dayOfMonth: Int?,
    val dayOfWeek: Int?,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class RecurringTransactionSummaryResponse(
    val totalActive: Int,
    val totalInactive: Int,
    val monthlyTotal: String,
    val yearlyTotal: String,
    val nextDueDate: String?,
    val transactions: List<RecurringTransactionResponse>
)

// ==================== DEBT RESPONSES ====================

@Serializable
data class DebtResponse(
    val id: String,
    val type: String,
    val personName: String,
    val personContact: String?,
    val amount: String,
    val remainingAmount: String,
    val paidAmount: String,
    val paidPercentage: String,
    val interestRate: String,
    val description: String?,
    val dueDate: String?,
    val status: String,
    val startDate: String,
    val daysOverdue: Int,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class DebtPaymentResponse(
    val id: String,
    val debtId: String,
    val amount: String,
    val paymentDate: String,
    val notes: String?,
    val createdAt: String
)

@Serializable
data class DebtSummaryResponse(
    val totalBorrowed: String,
    val totalLent: String,
    val totalBorrowedRemaining: String,
    val totalLentRemaining: String,
    val totalOverdue: Int,
    val borrowedDebts: List<DebtResponse>,
    val lentDebts: List<DebtResponse>
)

@Serializable
data class DebtDetailResponse(
    val debt: DebtResponse,
    val payments: List<DebtPaymentResponse>,
    val paymentCount: Int,
    val lastPaymentDate: String?
)

