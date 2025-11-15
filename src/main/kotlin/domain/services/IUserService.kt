package com.financial.domain.services

import com.financial.data.model.Profile
import com.financial.data.model.User
import com.financial.dtos.request.ChangePasswordRequest
import com.financial.dtos.request.UpdateProfileRequest
import com.financial.dtos.response.ProfileResponse
import com.financial.dtos.response.UploadAvatarResponse
import com.financial.dtos.response.UserResponse
import java.io.InputStream
import java.util.*

interface IUserService {
    /**
     * Get user profile
     */
    suspend fun getProfile(userId: UUID): ProfileResponse

    /**
     * Update user profile
     */
    suspend fun updateProfile(userId: UUID, request: UpdateProfileRequest): ProfileResponse

    /**
     * Change user password
     */
    suspend fun changePassword(userId: UUID, request: ChangePasswordRequest): Boolean

    /**
     * Upload avatar image
     */
    suspend fun uploadAvatar(
        userId: UUID,
        inputStream: InputStream,
        fileName: String,
        contentType: String,
        fileSize: Long
    ): UploadAvatarResponse
}

