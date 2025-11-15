package com.financial

import com.financial.data.database.DatabaseFactory
import com.financial.data.repository.impl.*
import com.financial.domain.service.impl.*
import com.financial.domain.services.impl.PasswordService
import com.financial.domain.services.impl.EmailService
import com.financial.plugins.*
import io.ktor.server.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.minio.MinioClient
import kotlinx.serialization.json.Json

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

    // HTTP Client for OAuth2
    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    // Email Service Setup
    val emailService = EmailService(
        smtpHost = config.propertyOrNull("email.smtp.host")?.getString() ?: "smtp.gmail.com",
        smtpPort = config.propertyOrNull("email.smtp.port")?.getString()?.toIntOrNull() ?: 465,
        smtpUsername = config.propertyOrNull("email.username")?.getString() ?: "",
        smtpPassword = config.propertyOrNull("email.password")?.getString() ?: "",
        fromEmail = config.propertyOrNull("email.from")?.getString() ?: "noreply@financial.app",
        fromName = config.propertyOrNull("email.fromName")?.getString() ?: "Financial App",
        frontendUrl = config.propertyOrNull("app.frontendUrl")?.getString() ?: "http://localhost:3000"
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
        categoryService = categoryService,
        emailService = emailService,
        httpClient = httpClient
    )

    val userService = UserService(
        userRepository = UserRepository(),
        profileRepository = ProfileRepository(),
        passwordService = PasswordService(),
        storageService = storageService
    )

    // Budget Service
    val budgetService = BudgetService(
        budgetRepository = BudgetRepository(),
        categoryRepository = CategoryRepository()
    )

    // Recurring Transaction Service
    val recurringTransactionService = RecurringTransactionService(
        recurringTransactionRepository = RecurringTransactionRepository(),
        categoryRepository = CategoryRepository(),
        transactionRepository = TransactionRepository()
    )

    // Debt Service
    val debtService = DebtService(
        debtRepository = DebtRepository(),
        debtPaymentRepository = DebtPaymentRepository()
    )

    configureSerialization()
    configureStatusPages()
    configureSecurity(jwtService)
    configureMonitoring()
    configureHTTP()
    configureRouting(authService, userService, categoryService, transactionService, budgetService, recurringTransactionService, debtService)
}
