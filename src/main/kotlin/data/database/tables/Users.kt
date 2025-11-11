package com.financial.data.database.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.or
import java.time.LocalDateTime

object Users : UUIDTable("users") {
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 255).nullable()
    val idFacebook = varchar("id_facebook", 255).uniqueIndex().nullable()
    val idGoogle = varchar("id_google", 255).uniqueIndex().nullable()
    val role = varchar("role", 20).default("user")
    val isBlocked = bool("is_blocked").default(false)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())

    init {
        check("valid_auth") {
            (passwordHash.isNotNull() or idFacebook.isNotNull() or idGoogle.isNotNull())
        }
        check("valid_role") {
            role eq "user" or (role eq "admin")
        }
        check("valid_email") {
            email like "%@%.%" // basic check; bạn có thể validate sâu hơn ở app layer
        }
    }
}