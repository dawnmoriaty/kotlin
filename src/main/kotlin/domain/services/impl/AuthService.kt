package com.financial.domain.service.impl

import com.financial.data.model.User
import com.financial.data.repository.IProfileRepository
import com.financial.data.repository.IRefreshTokenRepository
import com.financial.data.repository.IUserRepository
import com.financial.domain.exceptions.AuthException
import com.financial.domain.services.IAuthService
import com.financial.domain.services.ICategoryService
import com.financial.domain.services.IJwtService
import com.financial.domain.services.impl.PasswordService
import com.financial.dtos.AuthResult
import com.financial.dtos.LoginRequest
import com.financial.dtos.RegisterRequest
import com.financial.dtos.response.UserResponse
import java.time.Instant

class AuthService(
    private val userRepository: IUserRepository,
    private val passwordService: PasswordService,
    private val jwtService: IJwtService,
    private val refreshTokenRepository: IRefreshTokenRepository,
    private val profileRepository: IProfileRepository,
    private val categoryService: ICategoryService
) : IAuthService {

    override suspend fun register(registerRequest: RegisterRequest): AuthResult {
        // Check if email already exists
        if (userRepository.findByEmail(registerRequest.email) != null) {
            throw AuthException("Email already exists")
        }

        // Check if username already exists
        if (userRepository.findByUsername(registerRequest.username) != null) {
            throw AuthException("Username already exists")
        }

        // Create user
        val user = userRepository.create(
            username = registerRequest.username,
            email = registerRequest.email,
            passwordHash = passwordService.hashPassword(registerRequest.password)
        )

        // ✅ Automatically create a default empty profile for the new user
        profileRepository.create(user.id)

        // ✅ Automatically create default categories for the new user
        categoryService.createDefaultCategories(user.id)

        return createAuthResult(user)
    }

    override suspend fun login(loginRequest: LoginRequest): AuthResult {
        val user = userRepository.findByEmail(loginRequest.identifier)
            ?: userRepository.findByUsername(loginRequest.identifier)
            ?: throw AuthException("Invalid credentials")

        if (user.passwordHash == null) {
            throw AuthException("This account uses social login")
        }

        if (!passwordService.verifyPassword(loginRequest.password, user.passwordHash)) {
            throw AuthException("Invalid credentials")
        }

        return createAuthResult(user)
    }

    override suspend fun loginWithGoogle(googleId: String, email: String, username: String): AuthResult {
        var user = userRepository.findByGoogleId(googleId)
        if (user == null) {
            user = userRepository.create(username = username, email = email, idGoogle = googleId)
            // ✅ Create profile for new Google user
            profileRepository.create(user.id)
            // ✅ Create default categories
            categoryService.createDefaultCategories(user.id)
        }
        return createAuthResult(user)
    }

    override suspend fun loginWithFacebook(facebookId: String, email: String, username: String): AuthResult {
        var user = userRepository.findByFacebookId(facebookId)
        if (user == null) {
            user = userRepository.create(username = username, email = email, idFacebook = facebookId)
            // ✅ Create profile for new Facebook user
            profileRepository.create(user.id)
            // ✅ Create default categories
            categoryService.createDefaultCategories(user.id)
        }
        return createAuthResult(user)
    }

    override suspend fun refreshToken(refreshToken: String): AuthResult {
        // 1. Validate JWT format and type
        val userId = jwtService.validateRefreshToken(refreshToken)

        // 2. Check if token exists in database and hasn't been revoked
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
        // 1. Validate JWT format and type
        jwtService.validateRefreshToken(refreshToken)

        // 2. Verify token belongs to database
        if (!refreshTokenRepository.isTokenValid(refreshToken)) {
            throw AuthException("Refresh token is invalid or has already been revoked")
        }

        // 3. Revoke token in database
        val revoked = refreshTokenRepository.revokeToken(refreshToken)

        if (!revoked) {
            throw AuthException("Failed to revoke refresh token")
        }

        return true
    }

    private suspend fun createAuthResult(user: User): AuthResult {
        // 1. Convert User to UserResponse
        val userResponse = UserResponse(
            id = user.id.toString(),
            username = user.username,
            email = user.email,
            role = user.role,
            idGoogle = user.idGoogle != null,
            idFacebook = user.idFacebook != null
        )

        // 2. Generate access token
        val accessToken = jwtService.generateAccessToken(
            userId = user.id.toString(),
            email = user.email,
            username = user.username,
            role = user.role,
            isGoogle = user.idGoogle != null,
            isFacebook = user.idFacebook != null
        )

        // 3. Generate refresh token
        val refreshToken = jwtService.generateRefreshToken(user.id)

        // 4. Save refresh token to database
        val expiresAt = Instant.now().plusMillis(7 * 24 * 60 * 60 * 1000) // 7 days
        refreshTokenRepository.saveToken(user.id, refreshToken, expiresAt)

        return AuthResult(
            user = userResponse,
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }
}
