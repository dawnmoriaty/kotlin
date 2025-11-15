package com.financial.data.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object PasswordResetTokens : Table("password_reset_tokens") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id").references(Users.id)
    val token = text("token").uniqueIndex()
    val expiresAt = timestamp("expires_at")
    val isUsed = bool("is_used").default(false)
    val createdAt = timestamp("created_at").default(Instant.now())

    override val primaryKey = PrimaryKey(id)
}
