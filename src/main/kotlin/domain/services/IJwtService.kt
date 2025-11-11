package com.financial.domain.services

import com.auth0.jwt.algorithms.Algorithm
import com.financial.dtos.JwtPayload
import java.util.UUID

interface IJwtService {
    val issuer: String
    val audience: String
    val algorithm: Algorithm

    fun generateAccessToken(userId: String, email: String, username: String, role: String, isGoogle: Boolean, isFacebook: Boolean): String
    fun generateRefreshToken(userId: UUID): String
    fun validateAccessToken(token: String): JwtPayload
    fun validateRefreshToken(token: String): UUID
}
