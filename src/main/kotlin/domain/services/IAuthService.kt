package com.financial.domain.services

import com.financial.dtos.AuthResult
import com.financial.dtos.LoginRequest
import com.financial.dtos.RegisterRequest

interface IAuthService {
    suspend fun register(registerRequest: RegisterRequest): AuthResult
    suspend fun login(loginRequest: LoginRequest): AuthResult
    suspend fun loginWithGoogle(googleId: String, email: String, username: String): AuthResult
    suspend fun loginWithFacebook(facebookId: String, email: String, username: String): AuthResult
    suspend fun refreshToken(refreshToken: String): AuthResult
    suspend fun logout(refreshToken: String): Boolean
}