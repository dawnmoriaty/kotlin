package com.financial.routes

import com.financial.domain.services.IDebtService
import com.financial.dtos.request.CreateDebtPaymentRequest
import com.financial.dtos.request.CreateDebtRequest
import com.financial.dtos.request.UpdateDebtRequest
import com.financial.dtos.response.ApiResponse
import com.financial.plugins.getUserId
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.debtRoutes(debtService: IDebtService) {
    route("/api/v1/debts") {

        // Get all debts
        get {
            val userId = call.getUserId()
            val type = call.request.queryParameters["type"] // borrowed or lent
            val debts = debtService.getDebts(userId, type)
            call.respond(HttpStatusCode.OK, ApiResponse.success(debts))
        }

        // Get overdue debts
        get("/overdue") {
            val userId = call.getUserId()
            val overdueDebts = debtService.getOverdueDebts(userId)
            call.respond(HttpStatusCode.OK, ApiResponse.success(overdueDebts))
        }

        // Get debt summary
        get("/summary") {
            val userId = call.getUserId()
            val summary = debtService.getDebtSummary(userId)
            call.respond(HttpStatusCode.OK, ApiResponse.success(summary))
        }

        // Get debt by ID
        get("/{id}") {
            val userId = call.getUserId()
            val debtId = UUID.fromString(call.parameters["id"]!!)
            val debt = debtService.getDebtById(userId, debtId)

            if (debt == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse.error<Unit>("Debt not found"))
            } else {
                call.respond(HttpStatusCode.OK, ApiResponse.success(debt))
            }
        }

        // Get debt detail (with payments)
        get("/{id}/detail") {
            val userId = call.getUserId()
            val debtId = UUID.fromString(call.parameters["id"]!!)
            val detail = debtService.getDebtDetail(userId, debtId)

            if (detail == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse.error<Unit>("Debt not found"))
            } else {
                call.respond(HttpStatusCode.OK, ApiResponse.success(detail))
            }
        }

        // Create debt
        post {
            val userId = call.getUserId()
            val request = call.receive<CreateDebtRequest>()
            val debt = debtService.createDebt(userId, request)
            call.respond(HttpStatusCode.Created, ApiResponse.success(debt, "Debt created successfully"))
        }

        // Update debt
        put("/{id}") {
            val userId = call.getUserId()
            val debtId = UUID.fromString(call.parameters["id"]!!)
            val request = call.receive<UpdateDebtRequest>()
            val debt = debtService.updateDebt(userId, debtId, request)

            if (debt == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse.error<Unit>("Debt not found"))
            } else {
                call.respond(HttpStatusCode.OK, ApiResponse.success(debt, "Debt updated successfully"))
            }
        }

        // Delete debt
        delete("/{id}") {
            val userId = call.getUserId()
            val debtId = UUID.fromString(call.parameters["id"]!!)
            val deleted = debtService.deleteDebt(userId, debtId)

            if (deleted) {
                call.respond(HttpStatusCode.OK, ApiResponse.success<Unit>(message = "Debt deleted successfully"))
            } else {
                call.respond(HttpStatusCode.NotFound, ApiResponse.error<Unit>("Debt not found"))
            }
        }

        // Add payment to debt
        post("/{id}/payments") {
            val userId = call.getUserId()
            val debtId = UUID.fromString(call.parameters["id"]!!)
            val request = call.receive<CreateDebtPaymentRequest>()
            val payment = debtService.addPayment(userId, debtId, request)
            call.respond(HttpStatusCode.Created, ApiResponse.success(payment, "Payment added successfully"))
        }

        // Get all payments for a debt
        get("/{id}/payments") {
            val userId = call.getUserId()
            val debtId = UUID.fromString(call.parameters["id"]!!)
            val payments = debtService.getPayments(userId, debtId)
            call.respond(HttpStatusCode.OK, ApiResponse.success(payments))
        }

        // Delete payment
        delete("/payments/{paymentId}") {
            val userId = call.getUserId()
            val paymentId = UUID.fromString(call.parameters["paymentId"]!!)

            // Note: debtId needs to be passed in request body or query param
            val debtId = UUID.fromString(call.request.queryParameters["debtId"]!!)

            val deleted = debtService.deletePayment(userId, debtId, paymentId)

            if (deleted) {
                call.respond(HttpStatusCode.OK, ApiResponse.success<Unit>(message = "Payment deleted successfully"))
            } else {
                call.respond(HttpStatusCode.NotFound, ApiResponse.error<Unit>("Payment not found"))
            }
        }
    }
}

