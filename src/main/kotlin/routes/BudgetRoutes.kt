package com.financial.routes

import com.financial.domain.services.IBudgetService
import com.financial.dtos.request.CreateBudgetRequest
import com.financial.dtos.request.UpdateBudgetRequest
import com.financial.dtos.response.ApiResponse
import com.financial.plugins.getUserId
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.budgetRoutes(budgetService: IBudgetService) {
    route("/api/v1/budgets") {

        // Get all budgets
        get {
            val userId = call.getUserId()
            val budgets = budgetService.getBudgets(userId)
            call.respond(HttpStatusCode.OK, ApiResponse.success(budgets))
        }

        // Get budget spending
        get("/spending") {
            val userId = call.getUserId()
            val period = call.request.queryParameters["period"]
            val spending = budgetService.getBudgetSpending(userId, period)
            call.respond(HttpStatusCode.OK, ApiResponse.success(spending))
        }

        // Get budget summary
        get("/summary") {
            val userId = call.getUserId()
            val summary = budgetService.getBudgetSummary(userId)
            call.respond(HttpStatusCode.OK, ApiResponse.success(summary))
        }

        // Get budget by ID
        get("/{id}") {
            val userId = call.getUserId()
            val budgetId = UUID.fromString(call.parameters["id"]!!)
            val budget = budgetService.getBudgetById(userId, budgetId)

            if (budget == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse.error<Unit>("Budget not found"))
            } else {
                call.respond(HttpStatusCode.OK, ApiResponse.success(budget))
            }
        }

        // Create budget
        post {
            val userId = call.getUserId()
            val request = call.receive<CreateBudgetRequest>()
            val budget = budgetService.createBudget(userId, request)
            call.respond(HttpStatusCode.Created, ApiResponse.success(budget, "Budget created successfully"))
        }

        // Update budget
        put("/{id}") {
            val userId = call.getUserId()
            val budgetId = UUID.fromString(call.parameters["id"]!!)
            val request = call.receive<UpdateBudgetRequest>()
            val budget = budgetService.updateBudget(userId, budgetId, request)

            if (budget == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse.error<Unit>("Budget not found"))
            } else {
                call.respond(HttpStatusCode.OK, ApiResponse.success(budget, "Budget updated successfully"))
            }
        }

        // Delete budget
        delete("/{id}") {
            val userId = call.getUserId()
            val budgetId = UUID.fromString(call.parameters["id"]!!)
            val deleted = budgetService.deleteBudget(userId, budgetId)

            if (deleted) {
                call.respond(HttpStatusCode.OK, ApiResponse.success<Unit>(message = "Budget deleted successfully"))
            } else {
                call.respond(HttpStatusCode.NotFound, ApiResponse.error<Unit>("Budget not found"))
            }
        }
    }
}

