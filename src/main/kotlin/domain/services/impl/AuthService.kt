package com.financial.domain.service.impl

import com.financial.data.database.tables.PasswordResetTokens
import com.financial.data.model.User
import com.financial.data.repository.IProfileRepository
import com.financial.data.repository.IRefreshTokenRepository
import com.financial.data.repository.IUserRepository
import com.financial.domain.exceptions.AuthException
import com.financial.domain.services.IAuthService
import com.financial.domain.services.ICategoryService
import com.financial.domain.services.IEmailService
import com.financial.domain.services.IJwtService
import com.financial.domain.services.impl.PasswordService
import com.financial.dtos.AuthResult
import com.financial.dtos.LoginRequest
import com.financial.dtos.RegisterRequest
import com.financial.dtos.response.UserResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.*

class AuthService(
    private val userRepository: IUserRepository,
    private val passwordService: PasswordService,
    private val jwtService: IJwtService,
    private val refreshTokenRepository: IRefreshTokenRepository,
    private val profileRepository: IProfileRepository,
    private val categoryService: ICategoryService,
    private val emailService: IEmailService,
    private val httpClient: HttpClient
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

    override suspend fun loginWithGoogle(idToken: String): AuthResult {
        // Verify Google ID Token
        val googleUserInfo = verifyGoogleToken(idToken)

        var user = userRepository.findByGoogleId(googleUserInfo.sub)
        if (user == null) {
            // Check if email already exists
            user = userRepository.findByEmail(googleUserInfo.email)
            if (user != null) {
                // Link Google account to existing user
                userRepository.linkGoogleAccount(user.id, googleUserInfo.sub)
            } else {
                // Create new user
                user = userRepository.create(
                    username = googleUserInfo.email.substringBefore("@"),
                    email = googleUserInfo.email,
                    idGoogle = googleUserInfo.sub
                )
                profileRepository.create(user.id)
                categoryService.createDefaultCategories(user.id)
            }
        }
        return createAuthResult(user)
    }

    override suspend fun loginWithFacebook(accessToken: String): AuthResult {
        // Verify Facebook Access Token
        val facebookUserInfo = verifyFacebookToken(accessToken)

        var user = userRepository.findByFacebookId(facebookUserInfo.id)
        if (user == null) {
            // Check if email already exists
            user = userRepository.findByEmail(facebookUserInfo.email)
            if (user != null) {
                // Link Facebook account to existing user
                userRepository.linkFacebookAccount(user.id, facebookUserInfo.id)
            } else {
                // Create new user
                user = userRepository.create(
                    username = facebookUserInfo.name.replace(" ", "_").lowercase(),
                    email = facebookUserInfo.email,
                    idFacebook = facebookUserInfo.id
                )
                profileRepository.create(user.id)
                categoryService.createDefaultCategories(user.id)
            }
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

    override suspend fun forgotPassword(email: String): Boolean {
        // Find user by email
        val user = userRepository.findByEmail(email) ?: throw AuthException("Email không tồn tại")

        // Check if user has password (not OAuth only)
        if (user.passwordHash == null) {
            throw AuthException("Tài khoản này sử dụng đăng nhập mạng xã hội. Vui lòng đăng nhập bằng Google hoặc Facebook.")
        }

        // Generate reset token
        val resetToken = UUID.randomUUID().toString()
        val expiresAt = Instant.now().plusSeconds(15 * 60) // 15 minutes

        // Save reset token to database
        newSuspendedTransaction {
            PasswordResetTokens.insert {
                it[userId] = user.id
                it[token] = resetToken
                it[PasswordResetTokens.expiresAt] = expiresAt
                it[isUsed] = false
            }
        }

        // Send email
        val emailSent = emailService.sendPasswordResetEmail(
            toEmail = user.email,
            resetToken = resetToken,
            userName = user.username
        )

        if (!emailSent) {
            throw AuthException("Không thể gửi email đặt lại mật khẩu")
        }

        return true
    }

    override suspend fun resetPassword(token: String, newPassword: String): Boolean {
        // Find token in database
        val resetTokenData = newSuspendedTransaction {
            PasswordResetTokens.select { PasswordResetTokens.token eq token }
                .singleOrNull()
        } ?: throw AuthException("Token không hợp lệ hoặc đã hết hạn")

        // Check if token is used
        if (resetTokenData[PasswordResetTokens.isUsed]) {
            throw AuthException("Token đã được sử dụng")
        }

        // Check if token is expired
        if (resetTokenData[PasswordResetTokens.expiresAt].isBefore(Instant.now())) {
            throw AuthException("Token đã hết hạn")
        }

        // Get user ID
        val userId = resetTokenData[PasswordResetTokens.userId]

        // Update password
        val hashedPassword = passwordService.hashPassword(newPassword)
        userRepository.updatePassword(userId, hashedPassword)

        // Mark token as used
        newSuspendedTransaction {
            PasswordResetTokens.update({ PasswordResetTokens.token eq token }) {
                it[isUsed] = true
            }
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

    // Helper methods for OAuth2
    @Serializable
    private data class GoogleUserInfo(
        val sub: String,
        val email: String,
        val name: String,
        @SerialName("email_verified") val emailVerified: Boolean
    )

    @Serializable
    private data class FacebookUserInfo(
        val id: String,
        val email: String,
        val name: String
    )

    private suspend fun verifyGoogleToken(idToken: String): GoogleUserInfo {
        try {
            val response: GoogleUserInfo = httpClient.get("https://oauth2.googleapis.com/tokeninfo") {
                parameter("id_token", idToken)
            }.body()

            if (!response.emailVerified) {
                throw AuthException("Email chưa được xác thực")
            }

            return response
        } catch (e: Exception) {
            throw AuthException("Google ID token không hợp lệ: ${e.message}")
        }
    }

    private suspend fun verifyFacebookToken(accessToken: String): FacebookUserInfo {
        try {
            val response: FacebookUserInfo = httpClient.get("https://graph.facebook.com/me") {
                parameter("access_token", accessToken)
                parameter("fields", "id,email,name")
            }.body()

            return response
        } catch (e: Exception) {
            throw AuthException("Facebook access token không hợp lệ: ${e.message}")
        }
    }
}
