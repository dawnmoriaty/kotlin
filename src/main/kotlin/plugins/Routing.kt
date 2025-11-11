package com.financial.plugins
import com.financial.domain.services.IAuthService
import com.financial.routes.authRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(authService: IAuthService) {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        authRoutes(authService)
    }
}
