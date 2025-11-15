package com.financial.data.repository.impl

import com.financial.data.database.tables.RefreshTokens
import com.financial.data.repository.IRefreshTokenRepository
import com.financial.dtos.RefreshTokenData
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant
import java.util.UUID

class RefreshTokenRepository : IRefreshTokenRepository {

    override suspend fun saveToken(userId: UUID, token: String, expiresAt: Instant): Boolean =
        newSuspendedTransaction {
            RefreshTokens.insert {
                it[RefreshTokens.userId] = userId
                it[RefreshTokens.token] = token
                it[RefreshTokens.expiresAt] = expiresAt
                it[RefreshTokens.isRevoked] = false
                it[RefreshTokens.createdAt] = Instant.now()
            }.insertedCount > 0
        }

    override suspend fun isTokenValid(token: String): Boolean =
        newSuspendedTransaction {
            RefreshTokens.selectAll().where {
                (RefreshTokens.token eq token) and
                        (RefreshTokens.isRevoked eq false) and
                        (RefreshTokens.expiresAt greater Instant.now())
            }.count() > 0
        }

    override suspend fun revokeToken(token: String): Boolean =
        newSuspendedTransaction {
            RefreshTokens.update({ RefreshTokens.token eq token }) {
                it[isRevoked] = true
                it[revokedAt] = Instant.now()
            } > 0
        }

    override suspend fun revokeAllUserTokens(userId: UUID): Boolean =
        newSuspendedTransaction {
            RefreshTokens.update({ RefreshTokens.userId eq userId }) {
                it[isRevoked] = true
                it[revokedAt] = Instant.now()
            } > 0
        }

    override suspend fun findByToken(token: String): RefreshTokenData? =
        newSuspendedTransaction {
            RefreshTokens.selectAll().where { RefreshTokens.token eq token }
                .map { rowToRefreshTokenData(it) }
                .singleOrNull()
        }

    private fun rowToRefreshTokenData(row: ResultRow): RefreshTokenData {
        return RefreshTokenData(
            id = row[RefreshTokens.id],
            userId = row[RefreshTokens.userId],
            token = row[RefreshTokens.token],
            expiresAt = row[RefreshTokens.expiresAt],
            isRevoked = row[RefreshTokens.isRevoked]
        )
    }
}

