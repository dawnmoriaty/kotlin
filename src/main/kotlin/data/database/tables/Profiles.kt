package com.financial.data.database.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Profiles : UUIDTable("profiles") {
    val userId = uuid("id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val fullName = varchar("full_name", 100).nullable()
    val avatarUrl = varchar("avatar_url", 500).nullable()
    val phone = varchar("phone", 20).nullable()
    val dateOfBirth = date("date_of_birth").nullable()
    val address = text("address").nullable()
    val bio = text("bio").nullable()
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}