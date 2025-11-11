package com.financial.data.database.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import java.math.BigDecimal
import java.time.LocalDateTime

object Transactions : UUIDTable("transactions") {
    val description = varchar("description", 200)
    val amount = decimal("amount", 18, 2).check { it greater BigDecimal.ZERO }
    val transactionDate = date("transaction_date")
    val categoryId = uuid("category_id").references(Categories.id, onDelete = ReferenceOption.RESTRICT)
    val userId = uuid("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}