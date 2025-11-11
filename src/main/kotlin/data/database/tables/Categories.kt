package com.financial.data.database.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.or
import java.time.LocalDateTime

object Categories : UUIDTable("categories") {
    val name = varchar("name", 100)
    val type = varchar("type", 10) // "income" | "expense"
    val icon = varchar("icon", 255).nullable()
    val userId = uuid("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val isDefault = bool("is_default").default(false)
    val createdAt = datetime("created_at").default(LocalDateTime.now())

    init {
        uniqueIndex(userId, name)
        check("valid_type") {
            type eq "income" or  (type eq "expense")
        }
    }
}