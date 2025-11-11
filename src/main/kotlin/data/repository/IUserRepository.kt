package com.financial.data.repository

import com.financial.data.model.User
import java.util.UUID

interface IUserRepository {
    suspend fun findById(id: UUID): User?
    suspend fun findByEmail(email: String): User?
    suspend fun findByUsername(username: String): User?
    suspend fun findByGoogleId(googleId: String): User?
    suspend fun findByFacebookId(facebookId: String): User?
    suspend fun create(
        username: String,
        email: String,
        passwordHash: String? = null,
        idGoogle: String? = null,
        idFacebook: String? = null
    ): User
    suspend fun update(user: User): User
    suspend fun delete(id: UUID): Boolean
}