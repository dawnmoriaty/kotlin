package com.financial.dtos.response

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val errors: List<String>? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun <T> success(data: T? = null, message: String = "Success"): ApiResponse<T> {
            return ApiResponse(
                success = true,
                message = message,
                data = data,
                errors = null
            )
        }

        fun <T> error(message: String, errors: List<String>? = null): ApiResponse<T> {
            return ApiResponse(
                success = false,
                message = message,
                data = null,
                errors = errors
            )
        }

        fun <T> error(message: String, error: String): ApiResponse<T> {
            return error(message, listOf(error))
        }
    }
}