package com.financial.domain.services.impl

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.financial.data.model.User
import java.time.Instant
import java.util.UUID

class JwtService(private val jwtSecret: String,
                 private val jwtIssuer: String,
                 private val jwtAudience: String,
                 private val expirationTimeMs: Long) {
    private val algorithm = Algorithm.HMAC256(jwtSecret)

    fun generateAccessToken(user: User): String {
        return JWT.create()
            .withSubject(user.id.toString())
            .withIssuer(jwtIssuer)
            .withAudience(jwtAudience)
            .withExpiresAt(Instant.now().plusMillis(expirationTimeMs))
            .withClaim("email", user.email)
            .withClaim("role", user.role)
            .sign(algorithm)
    }
    fun generateRefreshToken(user: User): String {
        return JWT.create()
            .withSubject(user.id.toString())
            .withIssuer(jwtIssuer)
            .withAudience(jwtAudience)
            .withExpiresAt(Instant.now().plusMillis(7 * 24 * 60 * 60 * 1000)) // 7 ngày
            .sign(algorithm)
    }
    fun validateToken(token: String): UUID {
        val verifier = JWT.require(algorithm)
            .withIssuer(jwtIssuer)
            .withAudience(jwtAudience)
            .build()

        val jwt = verifier.verify(token)
        return UUID.fromString(jwt.subject)
    }

}