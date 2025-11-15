package com.financial.dtos.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateCategoryRequest(
    val name: String? = null,
    val icon: String? = null
)

