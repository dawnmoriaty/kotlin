package com.financial.data.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class Profile(
    val userId: UUID,
    val fullName: String? = null,
    val avatarUrl: String? = null,
    val phone: String? = null,
    val dateOfBirth: LocalDate? = null,
    val address: String? = null,
    val bio: String? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
