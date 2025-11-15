package com.financial.domain.service.impl

import com.financial.data.repository.IProfileRepository
import com.financial.data.repository.IUserRepository
import com.financial.domain.exceptions.AuthException
import com.financial.domain.services.IStorageService
import com.financial.domain.services.IUserService
import com.financial.domain.services.impl.PasswordService
import com.financial.dtos.request.ChangePasswordRequest
import com.financial.dtos.request.UpdateProfileRequest
import com.financial.dtos.response.ProfileResponse
import com.financial.dtos.response.UploadAvatarResponse
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class UserService(
    private val userRepository: IUserRepository,
    private val profileRepository: IProfileRepository,
    private val passwordService: PasswordService,
    private val storageService: IStorageService
) : IUserService {


    override suspend fun getProfile(userId: UUID): ProfileResponse {
        val profile = profileRepository.findByUserId(userId)
            ?: throw AuthException("Profile not found")

        return ProfileResponse(
            userId = profile.userId.toString(),
            fullName = profile.fullName,
            avatarUrl = profile.avatarUrl,
            phone = profile.phone,
            dateOfBirth = profile.dateOfBirth?.toString(),
            address = profile.address,
            bio = profile.bio,
            createdAt = profile.createdAt.toString(),
            updatedAt = profile.updatedAt.toString()
        )
    }

    override suspend fun updateProfile(userId: UUID, request: UpdateProfileRequest): ProfileResponse {
        // Get current profile
        val currentProfile = profileRepository.findByUserId(userId)
            ?: throw AuthException("Profile not found")

        // Validate phone number if provided
        request.phone?.let { phone ->
            if (!isValidPhoneNumber(phone)) {
                throw IllegalArgumentException("Invalid phone number format")
            }
        }

        // Parse date of birth if provided
        val dateOfBirth = request.dateOfBirth?.let {
            try {
                LocalDate.parse(it)
            } catch (e: Exception) {
                throw IllegalArgumentException("Invalid date format. Use YYYY-MM-DD")
            }
        }

        // Update profile with new data
        val updatedProfile = currentProfile.copy(
            fullName = request.fullName ?: currentProfile.fullName,
            avatarUrl = request.avatarUrl ?: currentProfile.avatarUrl,
            phone = request.phone ?: currentProfile.phone,
            dateOfBirth = dateOfBirth ?: currentProfile.dateOfBirth,
            address = request.address ?: currentProfile.address,
            bio = request.bio ?: currentProfile.bio
        )

        val result = profileRepository.update(updatedProfile)

        return ProfileResponse(
            userId = result.userId.toString(),
            fullName = result.fullName,
            avatarUrl = result.avatarUrl,
            phone = result.phone,
            dateOfBirth = result.dateOfBirth?.toString(),
            address = result.address,
            bio = result.bio,
            createdAt = result.createdAt.toString(),
            updatedAt = result.updatedAt.toString()
        )
    }

    override suspend fun changePassword(userId: UUID, request: ChangePasswordRequest): Boolean {
        // Validate new password matches confirmation
        if (request.newPassword != request.confirmPassword) {
            throw IllegalArgumentException("New password and confirmation do not match")
        }

        // Validate new password strength
        if (request.newPassword.length < 8) {
            throw IllegalArgumentException("New password must be at least 8 characters long")
        }

        // Get user
        val user = userRepository.findById(userId)
            ?: throw AuthException("User not found")

        // Check if user has password (not social login only)
        if (user.passwordHash == null) {
            throw AuthException("Cannot change password for social login accounts")
        }

        // Verify current password
        if (!passwordService.verifyPassword(request.currentPassword, user.passwordHash)) {
            throw AuthException("Current password is incorrect")
        }

        // Check new password is different from current
        if (passwordService.verifyPassword(request.newPassword, user.passwordHash)) {
            throw IllegalArgumentException("New password must be different from current password")
        }

        // Hash new password and update user
        val newPasswordHash = passwordService.hashPassword(request.newPassword)
        val updatedUser = user.copy(passwordHash = newPasswordHash)

        userRepository.update(updatedUser)

        return true
    }

    override suspend fun uploadAvatar(
        userId: UUID,
        inputStream: InputStream,
        fileName: String,
        contentType: String,
        fileSize: Long
    ): UploadAvatarResponse {
        // Validate file type
        val allowedTypes = listOf("image/jpeg", "image/jpg", "image/png", "image/webp")
        if (contentType !in allowedTypes) {
            throw IllegalArgumentException("Invalid file type. Only JPEG, PNG, and WebP images are allowed")
        }

        // Validate file size (max 5MB)
        val maxSize = 5 * 1024 * 1024 // 5MB
        if (fileSize > maxSize) {
            throw IllegalArgumentException("File size too large. Maximum size is 5MB")
        }

        // Get current profile to delete old avatar if exists
        val currentProfile = profileRepository.findByUserId(userId)
            ?: throw AuthException("Profile not found")

        // Generate unique filename
        val uniqueFileName = storageService.generateUniqueFileName(fileName, userId)

        // Upload new avatar
        val avatarUrl = storageService.uploadFile(inputStream, uniqueFileName, contentType, fileSize)

        // Delete old avatar if exists
        currentProfile.avatarUrl?.let { oldUrl ->
            storageService.deleteFile(oldUrl)
        }

        // Update profile with new avatar URL
        val updatedProfile = currentProfile.copy(avatarUrl = avatarUrl)
        profileRepository.update(updatedProfile)

        return UploadAvatarResponse(
            avatarUrl = avatarUrl,
            fileName = uniqueFileName,
            uploadedAt = LocalDateTime.now().toString()
        )
    }

    /**
     * Validate phone number format (simple validation)
     * Format: 0901234567 (10 digits starting with 0)
     */
    private fun isValidPhoneNumber(phone: String): Boolean {
        val phoneRegex = Regex("^0\\d{9}$")
        return phoneRegex.matches(phone)
    }
}
