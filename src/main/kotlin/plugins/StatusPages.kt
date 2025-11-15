package com.financial.plugins

import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.financial.domain.exceptions.AuthException
import com.financial.dtos.response.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.SerializationException
import org.jetbrains.exposed.exceptions.ExposedSQLException

fun Application.configureStatusPages() {
    install(StatusPages) {
        // Authentication/Authorization Exceptions
        exception<AuthException> { call, cause ->
            call.application.log.warn("Authentication error: ${cause.message}")
            val statusCode = when {
                cause.message?.contains("invalid", ignoreCase = true) == true -> HttpStatusCode.Unauthorized
                cause.message?.contains("not found", ignoreCase = true) == true -> HttpStatusCode.NotFound
                cause.message?.contains("exists", ignoreCase = true) == true -> HttpStatusCode.Conflict
                else -> HttpStatusCode.BadRequest
            }
            call.respond(statusCode, ApiResponse.error<String>(cause.message ?: "Authentication failed"))
        }

        // JWT Token Exceptions
        exception<JWTDecodeException> { call, cause ->
            call.application.log.warn("JWT decode error: ${cause.message}")
            call.respond(HttpStatusCode.Unauthorized, ApiResponse.error<String>("Invalid or malformed token"))
        }

        exception<SignatureVerificationException> { call, cause ->
            call.application.log.warn("JWT signature verification failed: ${cause.message}")
            call.respond(HttpStatusCode.Unauthorized, ApiResponse.error<String>("Invalid token signature"))
        }

        exception<TokenExpiredException> { call, cause ->
            call.application.log.warn("JWT token expired: ${cause.message}")
            call.respond(HttpStatusCode.Unauthorized, ApiResponse.error<String>("Token has expired"))
        }

        // Serialization/Request Validation Exceptions
        exception<SerializationException> { call, cause ->
            call.application.log.warn("Serialization error: ${cause.message}")
            call.respond(HttpStatusCode.BadRequest, ApiResponse.error<String>("Invalid request body format"))
        }

        exception<IllegalArgumentException> { call, cause ->
            call.application.log.warn("Illegal argument: ${cause.message}")
            call.respond(HttpStatusCode.BadRequest, ApiResponse.error<String>(cause.message ?: "Bad request"))
        }

        // Database Exceptions
        exception<ExposedSQLException> { call, cause ->
            call.application.log.error("Database error occurred", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse.error<String>("An internal database error occurred. Please try again later.")
            )
        }

        // Catch-all for unexpected errors
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<String>("Internal server error"))
        }

        // 404 Not Found
        status(HttpStatusCode.NotFound) { call, _ ->
            call.respond(HttpStatusCode.NotFound, ApiResponse.error<String>("Route not found"))
        }
    }
}