package com.financial.dtos.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateCategoryRequest(
    val name: String,
    val type: String, // "income" or "expense"
    val icon: String? = null
)

