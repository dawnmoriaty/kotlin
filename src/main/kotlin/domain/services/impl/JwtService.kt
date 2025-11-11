package com.financial.domain.service.impl

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.financial.domain.services.IJwtService
import com.financial.dtos.JwtPayload
import java.time.Instant
import java.util.*

class JwtService(
    override val issuer: String,
    override val audience: String,
    private val secret: String,
    private val accessTokenExpirationMs: Long
) : IJwtService {

    override val algorithm: Algorithm = Algorithm.HMAC256(secret)

    override fun generateAccessToken(
        userId: String,
        email: String,
        username: String,
        role: String,
        isGoogle: Boolean,
        isFacebook: Boolean
    ): String {
        return JWT.create()
            .withSubject(userId.toString())
            .withIssuer(issuer)
            .withAudience(audience)
            .withExpiresAt(Instant.now().plusMillis(accessTokenExpirationMs))
            .withClaim("type", "access")
            .withClaim("email", email)
            .withClaim("username", username)
            .withClaim("role", role)
            .withClaim("isGoogle", isGoogle)
            .withClaim("isFacebook", isFacebook)
            .sign(algorithm)
    }

    override fun generateRefreshToken(userId: UUID): String {
        return JWT.create()
            .withSubject(userId.toString())
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("type", "refresh")
            .withExpiresAt(Instant.now().plusMillis(7 * 24 * 60 * 60 * 1000)) // 7 days
            .sign(algorithm)
    }

    override fun validateAccessToken(token: String): JwtPayload {
        val verifier = JWT.require(algorithm)
            .withIssuer(issuer)
            .withAudience(audience)
            .build()

        val jwt = verifier.verify(token)
        return JwtPayload(
            userId = UUID.fromString(jwt.subject),
            email = jwt.getClaim("email").asString(),
            username = jwt.getClaim("username").asString(),
            role = jwt.getClaim("role").asString(),
            isGoogle = jwt.getClaim("isGoogle").asBoolean(),
            isFacebook = jwt.getClaim("isFacebook").asBoolean()
        )
    }

    override fun validateRefreshToken(token: String): UUID {
        val verifier = JWT.require(algorithm)
            .withIssuer(issuer)
            .withAudience(audience)
            .build()
        val jwt = verifier.verify(token)

        // Validate token type
        val tokenType = jwt.getClaim("type").asString()
        if (tokenType != "refresh") {
            throw IllegalArgumentException("Invalid token type. Expected refresh token but got: $tokenType")
        }

        return UUID.fromString(jwt.subject)
    }
}