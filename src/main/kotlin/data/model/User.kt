package com.financial.data.model

import java.time.LocalDateTime
import java.util.UUID

data class User(
    val id: UUID,
    val username: String,
    val email: String,
    val passwordHash: String? = null,
    val idFacebook: String? = null,
    val idGoogle: String? = null,
    val role: String = "user",
    val isBlocked: Boolean = false,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
