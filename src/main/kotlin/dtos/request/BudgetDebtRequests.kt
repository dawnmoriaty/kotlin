package com.financial.dtos.request

import kotlinx.serialization.Serializable
import java.math.BigDecimal

// ==================== BUDGET REQUESTS ====================

@Serializable
data class CreateBudgetRequest(
    val categoryId: String,
    val amount: String, // BigDecimal as String
    val period: String, // 'daily', 'weekly', 'monthly', 'yearly'
    val startDate: String, // LocalDate as String (YYYY-MM-DD)
    val endDate: String? = null,
    val alertPercentage: String? = "80.00"
)

@Serializable
data class UpdateBudgetRequest(
    val amount: String? = null,
    val period: String? = null,
    val endDate: String? = null,
    val isActive: Boolean? = null,
    val alertPercentage: String? = null
)

// ==================== RECURRING TRANSACTION REQUESTS ====================

@Serializable
data class CreateRecurringTransactionRequest(
    val categoryId: String,
    val description: String,
    val amount: String,
    val frequency: String, // 'daily', 'weekly', 'monthly', 'yearly'
    val startDate: String,
    val endDate: String? = null,
    val autoCreate: Boolean = true,
    val dayOfMonth: Int? = null, // 1-31 for monthly
    val dayOfWeek: Int? = null   // 0-6 for weekly (0=Sunday)
)

@Serializable
data class UpdateRecurringTransactionRequest(
    val description: String? = null,
    val amount: String? = null,
    val frequency: String? = null,
    val endDate: String? = null,
    val isActive: Boolean? = null,
    val autoCreate: Boolean? = null,
    val dayOfMonth: Int? = null,
    val dayOfWeek: Int? = null
)

// ==================== DEBT REQUESTS ====================

@Serializable
data class CreateDebtRequest(
    val type: String, // 'borrowed' or 'lent'
    val personName: String,
    val personContact: String? = null,
    val amount: String,
    val interestRate: String? = "0",
    val description: String? = null,
    val dueDate: String? = null,
    val startDate: String? = null
)

@Serializable
data class UpdateDebtRequest(
    val personName: String? = null,
    val personContact: String? = null,
    val interestRate: String? = null,
    val description: String? = null,
    val dueDate: String? = null,
    val status: String? = null
)

@Serializable
data class CreateDebtPaymentRequest(
    val debtId: String,
    val amount: String,
    val paymentDate: String? = null,
    val notes: String? = null
)

