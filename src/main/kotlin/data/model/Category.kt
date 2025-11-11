package com.financial.data.model

import com.financial.data.database.enums.TransactionType
import java.time.LocalDateTime
import java.util.*


data class Category(
    val id: UUID,
    val name: String,
    val type: TransactionType,
    val icon: String? = null,
    val userId: UUID,
    val isDefault: Boolean = false,
    val createdAt: LocalDateTime
)
