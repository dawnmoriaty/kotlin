package com.financial.routes

import com.financial.domain.services.ICategoryService
import com.financial.dtos.request.CreateCategoryRequest
import com.financial.dtos.request.UpdateCategoryRequest
import com.financial.dtos.response.ApiResponse
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.categoryRoutes(categoryService: ICategoryService) {
    authenticate("auth-jwt") {
        route("/api/v1/categories") {
            // Get all categories
            get {
                val userId = call.principal<JWTPrincipal>()
                    ?.payload?.subject?.let { UUID.fromString(it) }
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse.error<String>("Invalid token")
                    )

                val type = call.request.queryParameters["type"] // Optional filter by type

                val categories = if (type != null) {
                    categoryService.getCategoriesByType(userId, type)
                } else {
                    categoryService.getAllCategories(userId)
                }

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.success(categories, "Categories retrieved successfully")
                )
            }

            // Get category by ID
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
                        ApiResponse.error<String>("Invalid category ID")
                    )

                val category = categoryService.getCategoryById(id, userId)
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.success(category, "Category retrieved successfully")
                )
            }

            // Create category
            post {
                val userId = call.principal<JWTPrincipal>()
                    ?.payload?.subject?.let { UUID.fromString(it) }
                    ?: return@post call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse.error<String>("Invalid token")
                    )

                val request = call.receive<CreateCategoryRequest>()
                val category = categoryService.createCategory(userId, request)

                call.respond(
                    HttpStatusCode.Created,
                    ApiResponse.success(category, "Category created successfully")
                )
            }

            // Update category
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
                        ApiResponse.error<String>("Invalid category ID")
                    )

                val request = call.receive<UpdateCategoryRequest>()
                val category = categoryService.updateCategory(id, userId, request)

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.success(category, "Category updated successfully")
                )
            }

            // Delete category
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
                        ApiResponse.error<String>("Invalid category ID")
                    )

                categoryService.deleteCategory(id, userId)

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.success<Unit>(message = "Category deleted successfully")
                )
            }
        }
    }
}

