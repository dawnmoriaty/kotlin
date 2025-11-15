package com.financial.data.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Budgets : Table("budgets") {
    val id = uuid("id")
    val userId = uuid("user_id").references(Users.id)
    val categoryId = uuid("category_id").references(Categories.id)
    val amount = decimal("amount", 18, 2)
    val period = varchar("period", 20).default("monthly")
    val startDate = date("start_date")
    val endDate = date("end_date").nullable()
    val isActive = bool("is_active").default(true)
    val alertPercentage = decimal("alert_percentage", 5, 2).default(80.toBigDecimal())
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())

    override val primaryKey = PrimaryKey(id)
}

object RecurringTransactions : Table("recurring_transactions") {
    val id = uuid("id")
    val userId = uuid("user_id").references(Users.id)
    val categoryId = uuid("category_id").references(Categories.id)
    val description = text("description")
    val amount = decimal("amount", 18, 2)
    val frequency = varchar("frequency", 20)
    val startDate = date("start_date")
    val endDate = date("end_date").nullable()
    val nextOccurrence = date("next_occurrence")
    val isActive = bool("is_active").default(true)
    val autoCreate = bool("auto_create").default(true)
    val dayOfMonth = integer("day_of_month").nullable()
    val dayOfWeek = integer("day_of_week").nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())

    override val primaryKey = PrimaryKey(id)
}

object Debts : Table("debts") {
    val id = uuid("id")
    val userId = uuid("user_id").references(Users.id)
    val type = varchar("type", 20)
    val personName = text("person_name")
    val personContact = text("person_contact").nullable()
    val amount = decimal("amount", 18, 2)
    val remainingAmount = decimal("remaining_amount", 18, 2)
    val interestRate = decimal("interest_rate", 5, 2).default(0.toBigDecimal())
    val description = text("description").nullable()
    val dueDate = date("due_date").nullable()
    val status = varchar("status", 20).default("active")
    val startDate = date("start_date")
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())

    override val primaryKey = PrimaryKey(id)
}

object DebtPayments : Table("debt_payments") {
    val id = uuid("id")
    val debtId = uuid("debt_id").references(Debts.id)
    val amount = decimal("amount", 18, 2)
    val paymentDate = date("payment_date")
    val notes = text("notes").nullable()
    val createdAt = timestamp("created_at").default(Instant.now())

    override val primaryKey = PrimaryKey(id)
}

