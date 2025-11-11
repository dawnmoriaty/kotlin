package com.financial

import com.financial.data.database.DatabaseFactory
import com.financial.data.repository.impl.UserRepository
import com.financial.data.repository.impl.RefreshTokenRepository
import com.financial.domain.service.impl.AuthService
import com.financial.domain.service.impl.JwtService
import com.financial.domain.services.impl.PasswordService
import com.financial.plugins.configureHTTP
import com.financial.plugins.configureMonitoring
import com.financial.plugins.configureRouting
import com.financial.plugins.configureSecurity
import com.financial.plugins.configureSerialization
import io.ktor.server.application.*

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

    val authService = AuthService(
        userRepository = UserRepository(),
        passwordService = PasswordService(),
        jwtService = jwtService,
        refreshTokenRepository = RefreshTokenRepository()
    )

    configureSerialization()
    configureSecurity(jwtService)
    configureMonitoring()
    configureHTTP()
    configureRouting(authService)
}
