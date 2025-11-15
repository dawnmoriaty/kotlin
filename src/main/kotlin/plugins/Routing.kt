package com.financial.plugins

import com.financial.domain.services.*
import com.financial.routes.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    authService: IAuthService,
    userService: IUserService,
    categoryService: ICategoryService,
    transactionService: ITransactionService,
    budgetService: IBudgetService,
    recurringTransactionService: IRecurringTransactionService,
    debtService: IDebtService
) {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        authRoutes(authService)
        userRoutes(userService)
        categoryRoutes(categoryService)
        transactionRoutes(transactionService)
        budgetRoutes(budgetService)
        recurringTransactionRoutes(recurringTransactionService)
        debtRoutes(debtService)
    }
}
