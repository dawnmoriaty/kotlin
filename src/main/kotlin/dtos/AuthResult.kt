package com.financial.dtos

import com.financial.dtos.response.UserResponse
import kotlinx.serialization.Serializable

@Serializable
data class AuthResult(
    val user: UserResponse,
    val accessToken: String,
    val refreshToken: String
)
