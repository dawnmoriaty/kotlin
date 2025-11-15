package com.financial.dtos.response

import kotlinx.serialization.Serializable

@Serializable
data class TransactionStatistics(
    val totalIncome: String, // BigDecimal as string
    val totalExpense: String,
    val balance: String, // income - expense
    val transactionCount: Int,
    val incomeCount: Int,
    val expenseCount: Int
)

@Serializable
data class CategoryStatistics(
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String?,
    val categoryType: String,
    val totalAmount: String,
    val transactionCount: Int,
    val percentage: String // % of total income or expense
)

@Serializable
data class MonthlyStatistics(
    val month: String, // "YYYY-MM"
    val totalIncome: String,
    val totalExpense: String,
    val balance: String,
    val transactionCount: Int
)

