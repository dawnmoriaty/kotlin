package com.financial.routes

import com.financial.domain.services.IAuthService
import com.financial.dtos.*
import com.financial.dtos.request.*
import com.financial.dtos.response.ApiResponse
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: IAuthService) {
    route("api/v1/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val result = authService.register(request)
            call.respond(HttpStatusCode.Created, ApiResponse.success(result, "User registered successfully"))
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val result = authService.login(request)
            call.respond(HttpStatusCode.OK, ApiResponse.success(result, "Login successful"))
        }

        post("/google") {
            val request = call.receive<GoogleAuthRequest>()
            val result = authService.loginWithGoogle(request.idToken)
            call.respond(HttpStatusCode.OK, ApiResponse.success(result, "Google login successful"))
        }

        post("/facebook") {
            val request = call.receive<FacebookAuthRequest>()
            val result = authService.loginWithFacebook(request.accessToken)
            call.respond(HttpStatusCode.OK, ApiResponse.success(result, "Facebook login successful"))
        }

        post("/refresh") {
            val request = call.receive<RefreshTokenRequest>()
            val result = authService.refreshToken(request.refreshToken)
            call.respond(HttpStatusCode.OK, ApiResponse.success(result, "Token refreshed successfully"))
        }

        post("/logout") {
            val request = call.receive<LogoutRequest>()
            authService.logout(request.refreshToken)
            call.respond(HttpStatusCode.OK, ApiResponse.success<Unit>(message = "Logged out successfully"))
        }

        post("/forgot-password") {
            val request = call.receive<ForgotPasswordRequest>()
            authService.forgotPassword(request.email)
            call.respond(HttpStatusCode.OK, ApiResponse.success<Unit>(message = "Email đặt lại mật khẩu đã được gửi"))
        }

        post("/reset-password") {
            val request = call.receive<ResetPasswordRequest>()

            // Validate password match
            if (request.newPassword != request.confirmPassword) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.error<Unit>("Mật khẩu xác nhận không khớp")
                )
                return@post
            }

            // Validate password strength
            if (request.newPassword.length < 8) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.error<Unit>("Mật khẩu phải có ít nhất 8 ký tự")
                )
                return@post
            }

            authService.resetPassword(request.token, request.newPassword)
            call.respond(HttpStatusCode.OK, ApiResponse.success<Unit>(message = "Đặt lại mật khẩu thành công"))
        }
    }
}