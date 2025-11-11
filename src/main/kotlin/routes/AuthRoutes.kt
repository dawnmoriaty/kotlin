package com.financial.routes

import com.financial.domain.services.IAuthService
import com.financial.dtos.*
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
    }
}