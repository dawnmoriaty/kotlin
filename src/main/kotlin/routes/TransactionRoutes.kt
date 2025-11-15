package com.financial.routes

import com.financial.domain.services.ITransactionService
import com.financial.dtos.request.CreateTransactionRequest
import com.financial.dtos.request.UpdateTransactionRequest
import com.financial.dtos.response.ApiResponse
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.transactionRoutes(transactionService: ITransactionService) {
    authenticate("auth-jwt") {
        // Dashboard endpoint
        get("/api/v1/dashboard") {
            val userId = call.principal<JWTPrincipal>()
                ?.payload?.subject?.let { UUID.fromString(it) }
                ?: return@get call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse.error<String>("Invalid token")
                )

            val dashboard = transactionService.getDashboard(userId)

            call.respond(
                HttpStatusCode.OK,
                ApiResponse.success(dashboard, "Dashboard retrieved successfully")
            )
        }

        route("/api/v1/transactions") {
            // Get all transactions with filters
            get {
                val userId = call.principal<JWTPrincipal>()
                    ?.payload?.subject?.let { UUID.fromString(it) }
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse.error<String>("Invalid token")
                    )

                val categoryId = call.request.queryParameters["categoryId"]?.let { UUID.fromString(it) }
                val startDate = call.request.queryParameters["startDate"]
                val endDate = call.request.queryParameters["endDate"]
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
                val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0

                val transactions = transactionService.getTransactions(
                    userId, categoryId, startDate, endDate, limit, offset
                )

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.success(transactions, "Transactions retrieved successfully")
                )
            }

            // Get transaction by ID
            get("/{id}") {
                val userId = call.principal<JWTPrincipal>()
                    ?.payload?.subject?.let { UUID.fromString(it) }
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse.error<String>("Invalid token")
                    )

                val id = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error<String>("Invalid transaction ID")
                    )

                val transaction = transactionService.getTransactionById(id, userId)
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.success(transaction, "Transaction retrieved successfully")
                )
            }

            // Create transaction
            post {
                val userId = call.principal<JWTPrincipal>()
                    ?.payload?.subject?.let { UUID.fromString(it) }
                    ?: return@post call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse.error<String>("Invalid token")
                    )

                val request = call.receive<CreateTransactionRequest>()
                val transaction = transactionService.createTransaction(userId, request)

                call.respond(
                    HttpStatusCode.Created,
                    ApiResponse.success(transaction, "Transaction created successfully")
                )
            }

            // Update transaction
            put("/{id}") {
                val userId = call.principal<JWTPrincipal>()
                    ?.payload?.subject?.let { UUID.fromString(it) }
                    ?: return@put call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse.error<String>("Invalid token")
                    )

                val id = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error<String>("Invalid transaction ID")
                    )

                val request = call.receive<UpdateTransactionRequest>()
                val transaction = transactionService.updateTransaction(id, userId, request)

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.success(transaction, "Transaction updated successfully")
                )
            }

            // Delete transaction
            delete("/{id}") {
                val userId = call.principal<JWTPrincipal>()
                    ?.payload?.subject?.let { UUID.fromString(it) }
                    ?: return@delete call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse.error<String>("Invalid token")
                    )

                val id = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error<String>("Invalid transaction ID")
                    )

                transactionService.deleteTransaction(id, userId)

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.success<Unit>(message = "Transaction deleted successfully")
                )
            }
        }

        // Statistics endpoints
        route("/api/v1/statistics") {
            // Get overall statistics
            get {
                val userId = call.principal<JWTPrincipal>()
                    ?.payload?.subject?.let { UUID.fromString(it) }
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse.error<String>("Invalid token")
                    )

                val startDate = call.request.queryParameters["startDate"]
                val endDate = call.request.queryParameters["endDate"]

                val statistics = transactionService.getStatistics(userId, startDate, endDate)

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.success(statistics, "Statistics retrieved successfully")
                )
            }

            // Get category statistics
            get("/categories") {
                val userId = call.principal<JWTPrincipal>()
                    ?.payload?.subject?.let { UUID.fromString(it) }
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse.error<String>("Invalid token")
                    )

                val startDate = call.request.queryParameters["startDate"]
                val endDate = call.request.queryParameters["endDate"]

                val statistics = transactionService.getCategoryStatistics(userId, startDate, endDate)

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.success(statistics, "Category statistics retrieved successfully")
                )
            }
        }
    }
}

