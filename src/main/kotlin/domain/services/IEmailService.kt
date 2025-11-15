package com.financial.domain.services

interface IEmailService {
    suspend fun sendPasswordResetEmail(toEmail: String, resetToken: String, userName: String): Boolean
    suspend fun sendWelcomeEmail(toEmail: String, userName: String): Boolean
}
