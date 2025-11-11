package com.financial.plugins

import com.auth0.jwt.JWT
import com.financial.domain.service.impl.JwtService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.respond

fun Application.configureSecurity(jwtService: JwtService) {
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT.require(jwtService.algorithm)
                    .withIssuer(jwtService.issuer)
                    .withAudience(jwtService.audience)
                    .build()
            )
            validate { credential ->
                // Chỉ validate, không parse payload ở đây
                if (credential.payload.subject != null) {
                    JWTPrincipal(credential.payload)
                } else null
            }
            challenge { defaultScheme, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Invalid or expired token")
            }
        }
    }
}
