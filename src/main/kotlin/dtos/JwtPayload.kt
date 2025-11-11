package com.financial.dtos

import java.util.UUID

data class JwtPayload(
    val userId: UUID,
    val email: String,
    val username: String,
    val role: String,
    val isGoogle: Boolean,
    val isFacebook: Boolean
)
