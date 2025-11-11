package com.financial.domain.service.impl

import com.financial.data.model.User
import com.financial.dtos.AuthResult
import com.financial.data.repository.IUserRepository
import com.financial.data.repository.IRefreshTokenRepository
import com.financial.domain.exceptions.AuthException
import com.financial.domain.services.IAuthService
import com.financial.domain.services.IJwtService
import com.financial.domain.services.impl.PasswordService
import com.financial.dtos.LoginRequest
import com.financial.dtos.RegisterRequest
import com.financial.dtos.response.UserResponse
import java.time.Instant

class AuthService(
    private val userRepository: IUserRepository,
    private val passwordService: PasswordService,
    private val jwtService: IJwtService,
    private val refreshTokenRepository: IRefreshTokenRepository
) : IAuthService {

    override suspend fun register(registerRequest: RegisterRequest): AuthResult {
        if (userRepository.findByEmail(registerRequest.email) != null) throw AuthException("Email exists")
        if (userRepository.findByUsername(registerRequest.username) != null) throw AuthException("Username exists")

        val user = userRepository.create(
            username = registerRequest.username,
            email = registerRequest.email,
            passwordHash = passwordService.hashPassword(registerRequest.password)
        )
        return createAuthResult(user)
    }

    override suspend fun login(loginRequest: LoginRequest): AuthResult {
        val user = userRepository.findByEmail(loginRequest.identifier)
            ?: userRepository.findByUsername(loginRequest.identifier)
            ?: throw IllegalArgumentException("Invalid credentials")

        if (user.passwordHash == null) {
            throw IllegalArgumentException("This account uses social login")
        }

        if (!passwordService.verifyPassword(loginRequest.password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid credentials")
        }

        return createAuthResult(user)
    }

    override suspend fun loginWithGoogle(googleId: String, email: String, username: String): AuthResult {
        var user = userRepository.findByGoogleId(googleId)
        if (user == null) {
            user = userRepository.create(username = username, email = email, idGoogle = googleId)
        }
        return createAuthResult(user)
    }

    override suspend fun loginWithFacebook(facebookId: String, email: String, username: String): AuthResult {
        var user = userRepository.findByFacebookId(facebookId)
        if (user == null) {
            user = userRepository.create(username = username, email = email, idFacebook = facebookId)
        }
        return createAuthResult(user)
    }

    override suspend fun refreshToken(refreshToken: String): AuthResult {
        // 1. Validate JWT format và type
        val userId = jwtService.validateRefreshToken(refreshToken)

        // 2. Check token có trong database và chưa bị revoke
        if (!refreshTokenRepository.isTokenValid(refreshToken)) {
            throw AuthException("Refresh token is invalid or has been revoked")
        }

        // 3. Get user
        val user = userRepository.findById(userId) ?: throw AuthException("User not found")

        // 4. Revoke old refresh token
        refreshTokenRepository.revokeToken(refreshToken)

        // 5. Create new tokens
        return createAuthResult(user)
    }

    override suspend fun logout(refreshToken: String): Boolean {
        try {
            // 1. Validate JWT format và type
            jwtService.validateRefreshToken(refreshToken)

            // 2. Revoke token in database
            return refreshTokenRepository.revokeToken(refreshToken)
        } catch (e: Exception) {
            throw AuthException("Invalid refresh token: ${e.message}")
        }
    }

    private suspend fun createAuthResult(user: User): AuthResult {
        // 1. Chuyển User → UserResponse
        val userResponse = UserResponse(
            id = user.id.toString(),
            username = user.username,
            email = user.email,
            role = user.role,
            idGoogle = user.idGoogle != null,
            idFacebook = user.idFacebook != null
        )

        // 2. Tạo access token
        val accessToken = jwtService.generateAccessToken(
            userId = user.id.toString(),
            email = user.email,
            username = user.username,
            role = user.role,
            isGoogle = user.idGoogle != null,
            isFacebook = user.idFacebook != null
        )

        // 3. Tạo refresh token
        val refreshToken = jwtService.generateRefreshToken(user.id)

        // 4. Lưu refresh token vào database
        val expiresAt = Instant.now().plusMillis(7 * 24 * 60 * 60 * 1000) // 7 days
        refreshTokenRepository.saveToken(user.id, refreshToken, expiresAt)

        return AuthResult(
            user = userResponse,
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }
    }