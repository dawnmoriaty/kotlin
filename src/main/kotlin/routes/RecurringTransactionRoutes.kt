package com.financial.routes

import com.financial.domain.services.IRecurringTransactionService
import com.financial.dtos.request.CreateRecurringTransactionRequest
import com.financial.dtos.request.UpdateRecurringTransactionRequest
import com.financial.dtos.response.ApiResponse
import com.financial.plugins.getUserId
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.recurringTransactionRoutes(recurringService: IRecurringTransactionService) {
    route("/api/v1/recurring-transactions") {

        // Get all recurring transactions
        get {
            val userId = call.getUserId()
            val transactions = recurringService.getRecurringTransactions(userId)
            call.respond(HttpStatusCode.OK, ApiResponse.success(transactions))
        }

        // Get due recurring transactions
        get("/due") {
            val userId = call.getUserId()
            val dueTransactions = recurringService.getDueRecurringTransactions(userId)
            call.respond(HttpStatusCode.OK, ApiResponse.success(dueTransactions))
        }

        // Get recurring summary
        get("/summary") {
            val userId = call.getUserId()
            val summary = recurringService.getRecurringSummary(userId)
            call.respond(HttpStatusCode.OK, ApiResponse.success(summary))
        }

        // Get recurring transaction by ID
        get("/{id}") {
            val userId = call.getUserId()
            val id = UUID.fromString(call.parameters["id"]!!)
            val transaction = recurringService.getRecurringTransactionById(userId, id)

            if (transaction == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse.error<Unit>("Recurring transaction not found"))
            } else {
                call.respond(HttpStatusCode.OK, ApiResponse.success(transaction))
            }
        }

        // Create recurring transaction
        post {
            val userId = call.getUserId()
            val request = call.receive<CreateRecurringTransactionRequest>()
            val transaction = recurringService.createRecurringTransaction(userId, request)
            call.respond(HttpStatusCode.Created, ApiResponse.success(transaction, "Recurring transaction created successfully"))
        }

        // Execute recurring transaction manually
        post("/{id}/execute") {
            val userId = call.getUserId()
            val id = UUID.fromString(call.parameters["id"]!!)
            val executed = recurringService.executeRecurringTransaction(userId, id)

            if (executed) {
                call.respond(HttpStatusCode.OK, ApiResponse.success<Unit>(message = "Recurring transaction executed successfully"))
            } else {
                call.respond(HttpStatusCode.NotFound, ApiResponse.error<Unit>("Recurring transaction not found"))
            }
        }

        // Update recurring transaction
        put("/{id}") {
            val userId = call.getUserId()
            val id = UUID.fromString(call.parameters["id"]!!)
            val request = call.receive<UpdateRecurringTransactionRequest>()
            val transaction = recurringService.updateRecurringTransaction(userId, id, request)

            if (transaction == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse.error<Unit>("Recurring transaction not found"))
            } else {
                call.respond(HttpStatusCode.OK, ApiResponse.success(transaction, "Recurring transaction updated successfully"))
            }
        }

        // Delete recurring transaction
        delete("/{id}") {
            val userId = call.getUserId()
            val id = UUID.fromString(call.parameters["id"]!!)
            val deleted = recurringService.deleteRecurringTransaction(userId, id)

            if (deleted) {
                call.respond(HttpStatusCode.OK, ApiResponse.success<Unit>(message = "Recurring transaction deleted successfully"))
            } else {
                call.respond(HttpStatusCode.NotFound, ApiResponse.error<Unit>("Recurring transaction not found"))
            }
        }
    }
}

