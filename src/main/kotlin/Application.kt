package com.financial

import com.financial.data.database.DatabaseFactory
import com.financial.data.repository.impl.UserRepository
import com.financial.data.repository.impl.RefreshTokenRepository
import com.financial.data.repository.impl.ProfileRepository
import com.financial.data.repository.impl.CategoryRepository
import com.financial.data.repository.impl.TransactionRepository
import com.financial.domain.service.impl.AuthService
import com.financial.domain.service.impl.CategoryService
import com.financial.domain.service.impl.JwtService
import com.financial.domain.service.impl.TransactionService
import com.financial.domain.service.impl.UserService
import com.financial.domain.service.impl.MinioStorageService
import com.financial.domain.services.impl.PasswordService
import com.financial.plugins.configureHTTP
import com.financial.plugins.configureMonitoring
import com.financial.plugins.configureRouting
import com.financial.plugins.configureSecurity
import com.financial.plugins.configureSerialization
import com.financial.plugins.configureStatusPages
import io.ktor.server.application.*
import io.minio.MinioClient

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val config = environment.config

    val dbHost = config.property("database.host").getString()
    val dbPort = config.property("database.port").getString().toInt()
    val dbName = config.property("database.name").getString()
    val dbUser = config.property("database.user").getString()
    val dbPassword = config.property("database.password").getString()
    val dbMaxPoolSize = config.property("database.maxPoolSize").getString().toIntOrNull() ?: 10

    DatabaseFactory.init(
        host = dbHost,
        port = dbPort,
        name = dbName,
        user = dbUser,
        password = dbPassword,
        maxPoolSize = dbMaxPoolSize
    )
    val jwtService = JwtService(
        issuer = config.property("jwt.issuer").getString(),
        audience = config.property("jwt.audience").getString(),
        secret = config.property("jwt.secret").getString(),
        accessTokenExpirationMs = config.property("jwt.expirationTime").getString().toLong()
    )

    // MinIO Client Setup
    val minioClient = MinioClient.builder()
        .endpoint(config.property("minio.endpoint").getString())
        .credentials(
            config.property("minio.accessKey").getString(),
            config.property("minio.secretKey").getString()
        )
        .build()

    val storageService = MinioStorageService(
        minioClient = minioClient,
        bucketName = config.property("minio.bucketName").getString(),
        publicUrl = config.property("minio.publicUrl").getString()
    )

    val categoryService = CategoryService(
        categoryRepository = CategoryRepository()
    )

    val transactionService = TransactionService(
        transactionRepository = TransactionRepository(),
        categoryRepository = CategoryRepository()
    )

    val authService = AuthService(
        userRepository = UserRepository(),
        passwordService = PasswordService(),
        jwtService = jwtService,
        refreshTokenRepository = RefreshTokenRepository(),
        profileRepository = ProfileRepository(),
        categoryService = categoryService
    )

    val userService = UserService(
        userRepository = UserRepository(),
        profileRepository = ProfileRepository(),
        passwordService = PasswordService(),
        storageService = storageService
    )

    configureSerialization()
    configureStatusPages()
    configureSecurity(jwtService)
    configureMonitoring()
    configureHTTP()
    configureRouting(authService, userService, categoryService, transactionService)
}
