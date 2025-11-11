package com.financial.data.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class Transaction(
    val id: UUID,
    val description: String,
    val amount: BigDecimal,
    val transactionDate: LocalDate,
    val categoryId: UUID,
    val userId: UUID,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
