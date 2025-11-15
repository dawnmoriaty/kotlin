package com.financial.data.repository.impl

import com.financial.data.database.tables.RecurringTransactions
import com.financial.data.model.RecurringTransaction
import com.financial.data.repository.IRecurringTransactionRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.*

class RecurringTransactionRepository : IRecurringTransactionRepository {

    override suspend fun findById(id: UUID): RecurringTransaction? = newSuspendedTransaction {
        RecurringTransactions.select { RecurringTransactions.id eq id }
            .singleOrNull()
            ?.toRecurringTransaction()
    }

    override suspend fun findByUserId(userId: UUID): List<RecurringTransaction> = newSuspendedTransaction {
        RecurringTransactions.select { RecurringTransactions.userId eq userId }
            .orderBy(RecurringTransactions.nextOccurrence to SortOrder.ASC)
            .map { it.toRecurringTransaction() }
    }

    override suspend fun findActiveByUserId(userId: UUID): List<RecurringTransaction> = newSuspendedTransaction {
        RecurringTransactions.select {
            (RecurringTransactions.userId eq userId) and
            (RecurringTransactions.isActive eq true)
        }
            .orderBy(RecurringTransactions.nextOccurrence to SortOrder.ASC)
            .map { it.toRecurringTransaction() }
    }

    override suspend fun findDueTransactions(currentDate: LocalDate): List<RecurringTransaction> = newSuspendedTransaction {
        RecurringTransactions.select {
            (RecurringTransactions.isActive eq true) and
            (RecurringTransactions.autoCreate eq true) and
            (RecurringTransactions.nextOccurrence lessEq currentDate)
        }
            .map { it.toRecurringTransaction() }
    }

    override suspend fun findDueByUserId(userId: UUID, currentDate: LocalDate): List<RecurringTransaction> = newSuspendedTransaction {
        RecurringTransactions.select {
            (RecurringTransactions.userId eq userId) and
            (RecurringTransactions.isActive eq true) and
            (RecurringTransactions.nextOccurrence lessEq currentDate)
        }
            .orderBy(RecurringTransactions.nextOccurrence to SortOrder.ASC)
            .map { it.toRecurringTransaction() }
    }

    override suspend fun create(
        userId: UUID,
        categoryId: UUID,
        description: String,
        amount: BigDecimal,
        frequency: String,
        startDate: LocalDate,
        endDate: LocalDate?,
        autoCreate: Boolean,
        dayOfMonth: Int?,
        dayOfWeek: Int?
    ): RecurringTransaction = newSuspendedTransaction {
        val nextOccurrence = calculateNextOccurrence(startDate, frequency, dayOfMonth, dayOfWeek)
        val recurringId = UUID.randomUUID()

        RecurringTransactions.insert {
            it[id] = recurringId
            it[RecurringTransactions.userId] = userId
            it[RecurringTransactions.categoryId] = categoryId
            it[RecurringTransactions.description] = description
            it[RecurringTransactions.amount] = amount
            it[RecurringTransactions.frequency] = frequency
            it[RecurringTransactions.startDate] = startDate
            it[RecurringTransactions.endDate] = endDate
            it[RecurringTransactions.nextOccurrence] = nextOccurrence
            it[isActive] = true
            it[RecurringTransactions.autoCreate] = autoCreate
            it[RecurringTransactions.dayOfMonth] = dayOfMonth
            it[RecurringTransactions.dayOfWeek] = dayOfWeek
            it[createdAt] = Instant.now()
            it[updatedAt] = Instant.now()
        }

        findById(recurringId)!!
    }

    override suspend fun update(
        id: UUID,
        description: String?,
        amount: BigDecimal?,
        frequency: String?,
        endDate: LocalDate?,
        isActive: Boolean?,
        autoCreate: Boolean?,
        dayOfMonth: Int?,
        dayOfWeek: Int?
    ): RecurringTransaction? = newSuspendedTransaction {
        val updated = RecurringTransactions.update({ RecurringTransactions.id eq id }) {
            description?.let { desc -> it[RecurringTransactions.description] = desc }
            amount?.let { amt -> it[RecurringTransactions.amount] = amt }
            frequency?.let { freq -> it[RecurringTransactions.frequency] = freq }
            endDate?.let { date -> it[RecurringTransactions.endDate] = date }
            isActive?.let { active -> it[RecurringTransactions.isActive] = active }
            autoCreate?.let { auto -> it[RecurringTransactions.autoCreate] = auto }
            dayOfMonth?.let { day -> it[RecurringTransactions.dayOfMonth] = day }
            dayOfWeek?.let { week -> it[RecurringTransactions.dayOfWeek] = week }
            it[updatedAt] = Instant.now()
        }

        if (updated > 0) findById(id) else null
    }

    override suspend fun updateNextOccurrence(id: UUID, nextOccurrence: LocalDate): Boolean = newSuspendedTransaction {
        RecurringTransactions.update({ RecurringTransactions.id eq id }) {
            it[RecurringTransactions.nextOccurrence] = nextOccurrence
            it[updatedAt] = Instant.now()
        } > 0
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        RecurringTransactions.deleteWhere { RecurringTransactions.id eq id } > 0
    }

    private fun calculateNextOccurrence(
        startDate: LocalDate,
        frequency: String,
        dayOfMonth: Int?,
        dayOfWeek: Int?
    ): LocalDate {
        val today = LocalDate.now()
        var nextDate = if (startDate.isAfter(today)) startDate else today

        return when (frequency) {
            "daily" -> nextDate.plusDays(1)
            "weekly" -> {
                val targetDay = dayOfWeek ?: nextDate.dayOfWeek.value % 7
                while (nextDate.dayOfWeek.value % 7 != targetDay) {
                    nextDate = nextDate.plusDays(1)
                }
                if (!nextDate.isAfter(today)) nextDate.plusWeeks(1) else nextDate
            }
            "monthly" -> {
                val targetDay = dayOfMonth ?: nextDate.dayOfMonth
                nextDate = nextDate.withDayOfMonth(minOf(targetDay, nextDate.lengthOfMonth()))
                if (!nextDate.isAfter(today)) {
                    nextDate = nextDate.plusMonths(1)
                    nextDate = nextDate.withDayOfMonth(minOf(targetDay, nextDate.lengthOfMonth()))
                }
                nextDate
            }
            "yearly" -> {
                nextDate = LocalDate.of(nextDate.year, startDate.month, startDate.dayOfMonth)
                if (!nextDate.isAfter(today)) nextDate.plusYears(1) else nextDate
            }
            else -> nextDate.plusDays(1)
        }
    }

    private fun ResultRow.toRecurringTransaction() = RecurringTransaction(
        id = this[RecurringTransactions.id],
        userId = this[RecurringTransactions.userId],
        categoryId = this[RecurringTransactions.categoryId],
        description = this[RecurringTransactions.description],
        amount = this[RecurringTransactions.amount],
        frequency = this[RecurringTransactions.frequency],
        startDate = this[RecurringTransactions.startDate],
        endDate = this[RecurringTransactions.endDate],
        nextOccurrence = this[RecurringTransactions.nextOccurrence],
        isActive = this[RecurringTransactions.isActive],
        autoCreate = this[RecurringTransactions.autoCreate],
        dayOfMonth = this[RecurringTransactions.dayOfMonth],
        dayOfWeek = this[RecurringTransactions.dayOfWeek],
        createdAt = this[RecurringTransactions.createdAt],
        updatedAt = this[RecurringTransactions.updatedAt]
    )
}

