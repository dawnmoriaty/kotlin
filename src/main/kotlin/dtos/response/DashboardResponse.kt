package com.financial.dtos.response

import kotlinx.serialization.Serializable

@Serializable
data class DashboardResponse(
    val overview: OverviewStats,
    val recentTransactions: List<TransactionResponse>,
    val topExpenseCategories: List<CategoryStatistics>,
    val topIncomeCategories: List<CategoryStatistics>,
    val monthlyTrend: List<MonthlyStatistics>,
    val quickStats: QuickStats
)

@Serializable
data class OverviewStats(
    val totalIncome: String,
    val totalExpense: String,
    val balance: String,
    val incomeVsLastMonth: String, // Percentage change: "+15%" or "-10%"
    val expenseVsLastMonth: String
)

@Serializable
data class QuickStats(
    val totalTransactions: Int,
    val totalCategories: Int,
    val averageExpensePerDay: String,
    val averageIncomePerMonth: String,
    val largestExpense: String,
    val largestIncome: String
)

