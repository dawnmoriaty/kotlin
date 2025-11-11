package com.financial.dtos

import java.time.Instant
import java.util.UUID

data class RefreshTokenData(
    val id: UUID,
    val userId: UUID,
    val token: String,
    val expiresAt: Instant,
    val isRevoked: Boolean
)