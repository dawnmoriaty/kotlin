package com.financial.domain.services

import com.financial.dtos.AuthResult
import com.financial.dtos.LoginRequest
import com.financial.dtos.RegisterRequest

interface IAuthService {
    suspend fun register(registerRequest: RegisterRequest): AuthResult
    suspend fun login(loginRequest: LoginRequest): AuthResult
    suspend fun loginWithGoogle(idToken: String): AuthResult
    suspend fun loginWithFacebook(accessToken: String): AuthResult
    suspend fun refreshToken(refreshToken: String): AuthResult
    suspend fun logout(refreshToken: String): Boolean
    suspend fun forgotPassword(email: String): Boolean
    suspend fun resetPassword(token: String, newPassword: String): Boolean
}