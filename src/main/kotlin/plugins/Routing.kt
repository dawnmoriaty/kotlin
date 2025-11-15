package com.financial.plugins

import com.financial.domain.services.IAuthService
import com.financial.domain.services.ICategoryService
import com.financial.domain.services.ITransactionService
import com.financial.domain.services.IUserService
import com.financial.routes.authRoutes
import com.financial.routes.categoryRoutes
import com.financial.routes.transactionRoutes
import com.financial.routes.userRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    authService: IAuthService,
    userService: IUserService,
    categoryService: ICategoryService,
    transactionService: ITransactionService
) {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        authRoutes(authService)
        userRoutes(userService)
        categoryRoutes(categoryService)
        transactionRoutes(transactionService)
    }
}
