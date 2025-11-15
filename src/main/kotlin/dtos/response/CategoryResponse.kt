package com.financial.dtos.response

import kotlinx.serialization.Serializable

@Serializable
data class CategoryResponse(
    val id: String,
    val name: String,
    val type: String, // "income" or "expense"
    val icon: String? = null,
    val userId: String,
    val isDefault: Boolean,
    val createdAt: String
)

