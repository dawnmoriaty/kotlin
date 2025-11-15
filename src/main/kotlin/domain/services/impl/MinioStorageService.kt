package com.financial.domain.service.impl

import com.financial.domain.services.IStorageService
import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.*

class MinioStorageService(
    private val minioClient: MinioClient,
    private val bucketName: String,
    private val publicUrl: String
) : IStorageService {

    init {
        // Create bucket if not exists
        try {
            val exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build()
            )

            if (!exists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build()
                )
                println("✅ Created MinIO bucket: $bucketName")
            }
        } catch (e: Exception) {
            println("❌ Failed to initialize MinIO bucket: ${e.message}")
            throw e
        }
    }

    override suspend fun uploadFile(
        inputStream: InputStream,
        fileName: String,
        contentType: String,
        fileSize: Long
    ): String = withContext(Dispatchers.IO) {
        try {
            // Upload file to MinIO
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(fileName)
                    .stream(inputStream, fileSize, -1)
                    .contentType(contentType)
                    .build()
            )

            // Return public URL
            "$publicUrl/$fileName"
        } catch (e: Exception) {
            throw RuntimeException("Failed to upload file: ${e.message}", e)
        }
    }

    override suspend fun deleteFile(fileUrl: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Extract object name from URL
            val objectName = fileUrl.substringAfterLast("/")

            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(objectName)
                    .build()
            )

            true
        } catch (e: Exception) {
            println("⚠️ Failed to delete file: ${e.message}")
            false
        }
    }

    override fun generateUniqueFileName(originalFileName: String, userId: UUID): String {
        val timestamp = System.currentTimeMillis()
        val extension = originalFileName.substringAfterLast(".", "")
        val sanitizedName = originalFileName
            .substringBeforeLast(".")
            .replace(Regex("[^a-zA-Z0-9]"), "_")
            .take(50)

        return "avatars/${userId}/${sanitizedName}_${timestamp}.${extension}"
    }
}

