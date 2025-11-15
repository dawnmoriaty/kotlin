package com.financial.routes

import com.financial.domain.services.IUserService
import com.financial.dtos.request.ChangePasswordRequest
import com.financial.dtos.request.UpdateProfileRequest
import com.financial.dtos.response.ApiResponse
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.userRoutes(userService: IUserService) {
    authenticate("auth-jwt") {
        route("/api/v1/user") {

            // Change password
            put("/change-password") {
                val userId = call.principal<JWTPrincipal>()
                    ?.payload?.subject?.let { UUID.fromString(it) }
                    ?: return@put call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse.error<String>("Invalid token")
                    )

                val request = call.receive<ChangePasswordRequest>()
                userService.changePassword(userId, request)
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.success<Unit>(message = "Password changed successfully")
                )
            }
        }

        route("/api/v1/profile") {
            // Get user profile
            get {
                val userId = call.principal<JWTPrincipal>()
                    ?.payload?.subject?.let { UUID.fromString(it) }
                    ?: return@get call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse.error<String>("Invalid token")
                    )

                val profile = userService.getProfile(userId)
                call.respond(HttpStatusCode.OK, ApiResponse.success(profile, "Profile retrieved successfully"))
            }

            // Update user profile
            put {
                val userId = call.principal<JWTPrincipal>()
                    ?.payload?.subject?.let { UUID.fromString(it) }
                    ?: return@put call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse.error<String>("Invalid token")
                    )

                val request = call.receive<UpdateProfileRequest>()
                val updatedProfile = userService.updateProfile(userId, request)
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.success(updatedProfile, "Profile updated successfully")
                )
            }

            // Upload avatar
            post("/avatar") {
                val userId = call.principal<JWTPrincipal>()
                    ?.payload?.subject?.let { UUID.fromString(it) }
                    ?: return@post call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiResponse.error<String>("Invalid token")
                    )

                val multipart = call.receiveMultipart()
                var fileName: String? = null
                var fileBytes: ByteArray? = null
                var contentType: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            fileName = part.originalFileName ?: "avatar.jpg"
                            contentType = part.contentType?.toString() ?: "image/jpeg"
                            fileBytes = part.streamProvider().readBytes()
                        }
                        else -> part.dispose()
                    }
                }

                if (fileName == null || fileBytes == null) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error<String>("No file uploaded")
                    )
                }

                val inputStream = fileBytes!!.inputStream()
                val result = userService.uploadAvatar(
                    userId = userId,
                    inputStream = inputStream,
                    fileName = fileName!!,
                    contentType = contentType ?: "image/jpeg",
                    fileSize = fileBytes!!.size.toLong()
                )

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.success(result, "Avatar uploaded successfully")
                )
            }
        }
    }
}
