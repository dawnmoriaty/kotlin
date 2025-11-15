package com.financial.data.database

import com.financial.data.database.tables.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

object DatabaseFactory {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun init(
        host: String,
        port: Int,
        name: String,
        user: String,
        password: String,
        maxPoolSize: Int = 10
    ) {
        val jdbcUrl = "jdbc:postgresql://$host:$port/$name"

        val config = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.username = user
            this.password = password
            this.maximumPoolSize = maxPoolSize
            this.minimumIdle = 5
            this.connectionTimeout = 30000 // 30 seconds
            this.idleTimeout = 600000 // 10 minutes
            this.maxLifetime = 1800000 // 30 minutes
            this.connectionTestQuery = "SELECT 1"
            this.leakDetectionThreshold = 60000 // 60 seconds
            this.isAutoCommit = false
            validate()
        }

        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)

        // Test connection and create tables
        try {
            transaction {
                // Create tables if not exist
                SchemaUtils.create(Users, Profiles, Categories, Transactions, RefreshTokens)
                exec("SELECT 1")
            }
            logger.info("✅ Connected to PostgreSQL: $name @ $host:$port")
            logger.info("✅ Database tables created/verified")
        } catch (e: Exception) {
            logger.error("💥 Failed to connect to database", e)
            throw e
        }
    }
}