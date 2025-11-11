package com.financial.dtos.response

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: String,
    val username: String,
    val email: String,
    val role: String,
    val idGoogle: Boolean,
    val idFacebook: Boolean
)