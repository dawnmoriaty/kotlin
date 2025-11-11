package com.financial.data.repository

import com.financial.dtos.RefreshTokenData
import java.time.Instant
import java.util.UUID

interface IRefreshTokenRepository {
    suspend fun saveToken(userId: UUID, token: String, expiresAt: Instant): Boolean
    suspend fun isTokenValid(token: String): Boolean
    suspend fun revokeToken(token: String): Boolean
    suspend fun revokeAllUserTokens(userId: UUID): Boolean
    suspend fun findByToken(token: String): RefreshTokenData?
}



