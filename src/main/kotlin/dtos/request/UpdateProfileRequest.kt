package com.financial.dtos.request

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class UpdateProfileRequest(
    val fullName: String? = null,
    val avatarUrl: String? = null,
    val phone: String? = null,
    val dateOfBirth: String? = null, // Format: "YYYY-MM-DD"
    val address: String? = null,
    val bio: String? = null
)

