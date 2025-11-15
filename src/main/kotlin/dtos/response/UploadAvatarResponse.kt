package com.financial.dtos.response

import kotlinx.serialization.Serializable

@Serializable
data class UploadAvatarResponse(
    val avatarUrl: String,
    val fileName: String,
    val uploadedAt: String
)

