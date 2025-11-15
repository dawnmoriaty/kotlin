package com.financial.dtos.request

import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordRequest(
    val email: String
)

@Serializable
data class ResetPasswordRequest(
    val token: String,
    val newPassword: String,
    val confirmPassword: String
)

@Serializable
data class GoogleAuthRequest(
    val idToken: String
)

@Serializable
data class FacebookAuthRequest(
    val accessToken: String
)

