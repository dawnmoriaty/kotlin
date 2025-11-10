package com.financial

import com.financial.data.database.DatabaseFactory
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
    configureMonitoring()
    configureSerialization()
    configureSecurity()
    configureHTTP()
    configureRouting()
}
