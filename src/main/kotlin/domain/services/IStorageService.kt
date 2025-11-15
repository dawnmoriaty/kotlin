package com.financial.domain.services

import java.io.InputStream
import java.util.*

interface IStorageService {
    /**
     * Upload file to storage
     * @return Public URL of uploaded file
     */
    suspend fun uploadFile(
        inputStream: InputStream,
        fileName: String,
        contentType: String,
        fileSize: Long
    ): String

    /**
     * Delete file from storage
     */
    suspend fun deleteFile(fileUrl: String): Boolean

    /**
     * Generate unique filename to avoid conflicts
     */
    fun generateUniqueFileName(originalFileName: String, userId: UUID): String
}

