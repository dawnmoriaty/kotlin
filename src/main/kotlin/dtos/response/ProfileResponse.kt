package com.financial.dtos.response

import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponse(
    val userId: String,
    val fullName: String? = null,
    val avatarUrl: String? = null,
    val phone: String? = null,
    val dateOfBirth: String? = null, // Format: "YYYY-MM-DD"
    val address: String? = null,
    val bio: String? = null,
    val createdAt: String,
    val updatedAt: String
)

