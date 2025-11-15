package com.financial.dtos.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateTransactionRequest(
    val description: String? = null,
    val amount: String? = null, // String to BigDecimal
    val transactionDate: String? = null, // Format: "YYYY-MM-DD"
    val categoryId: String? = null // UUID string
)

