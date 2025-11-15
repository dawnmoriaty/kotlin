package com.financial.dtos.response

import kotlinx.serialization.Serializable

@Serializable
data class TransactionResponse(
    val id: String,
    val description: String,
    val amount: String, // BigDecimal as string
    val transactionDate: String, // "YYYY-MM-DD"
    val categoryId: String,
    val categoryName: String,
    val categoryType: String, // "income" or "expense"
    val categoryIcon: String?,
    val userId: String,
    val createdAt: String,
    val updatedAt: String
)

