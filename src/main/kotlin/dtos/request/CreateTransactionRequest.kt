package com.financial.dtos.request

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class CreateTransactionRequest(
    val description: String,
    val amount: String, // String để avoid serialization issues, sẽ parse to BigDecimal
    val transactionDate: String, // Format: "YYYY-MM-DD"
    val categoryId: String // UUID string
)

