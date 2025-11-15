package com.financial.data.repository.impl

import com.financial.data.database.tables.DebtPayments
import com.financial.data.database.tables.Debts
import com.financial.data.model.Debt
import com.financial.data.model.DebtPayment
import com.financial.data.model.DebtSummary
import com.financial.data.repository.IDebtPaymentRepository
import com.financial.data.repository.IDebtRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

class DebtRepository : IDebtRepository {

    override suspend fun findById(id: UUID): Debt? = newSuspendedTransaction {
        Debts.select { Debts.id eq id }
            .singleOrNull()
            ?.toDebt()
    }

    override suspend fun findByUserId(userId: UUID): List<Debt> = newSuspendedTransaction {
        Debts.select { Debts.userId eq userId }
            .orderBy(Debts.createdAt to SortOrder.DESC)
            .map { it.toDebt() }
    }

    override suspend fun findByUserIdAndType(userId: UUID, type: String): List<Debt> = newSuspendedTransaction {
        Debts.select {
            (Debts.userId eq userId) and (Debts.type eq type)
        }
            .orderBy(Debts.dueDate to SortOrder.ASC)
            .map { it.toDebt() }
    }

    override suspend fun findActiveByUserId(userId: UUID): List<Debt> = newSuspendedTransaction {
        Debts.select {
            (Debts.userId eq userId) and (Debts.status neq "paid")
        }
            .orderBy(Debts.dueDate to SortOrder.ASC)
            .map { it.toDebt() }
    }

    override suspend fun findOverdueByUserId(userId: UUID): List<Debt> = newSuspendedTransaction {
        Debts.select {
            (Debts.userId eq userId) and (Debts.status eq "overdue")
        }
            .orderBy(Debts.dueDate to SortOrder.ASC)
            .map { it.toDebt() }
    }

    override suspend fun getDebtSummary(debtId: UUID): DebtSummary? = newSuspendedTransaction {
        val debt = findById(debtId) ?: return@newSuspendedTransaction null

        val payments = DebtPayments.select { DebtPayments.debtId eq debtId }
            .map { it.toDebtPayment() }

        val paidAmount = debt.amount - debt.remainingAmount
        val paidPercentage = if (debt.amount > BigDecimal.ZERO) {
            (paidAmount.divide(debt.amount, 4, RoundingMode.HALF_UP) * BigDecimal(100))
                .setScale(2, RoundingMode.HALF_UP)
        } else BigDecimal.ZERO

        val daysOverdue = if (debt.dueDate != null && debt.dueDate.isBefore(LocalDate.now()) && debt.status != "paid") {
            ChronoUnit.DAYS.between(debt.dueDate, LocalDate.now()).toInt()
        } else 0

        DebtSummary(
            debt = debt,
            paidAmount = paidAmount,
            paidPercentage = paidPercentage,
            paymentCount = payments.size,
            lastPaymentDate = payments.maxOfOrNull { it.paymentDate },
            daysOverdue = daysOverdue
        )
    }

    override suspend fun getDebtSummaries(userId: UUID): List<DebtSummary> = newSuspendedTransaction {
        val debts = findByUserId(userId)
        debts.mapNotNull { debt -> getDebtSummary(debt.id) }
    }

    override suspend fun create(
        userId: UUID,
        type: String,
        personName: String,
        personContact: String?,
        amount: BigDecimal,
        interestRate: BigDecimal,
        description: String?,
        dueDate: LocalDate?,
        startDate: LocalDate
    ): Debt = newSuspendedTransaction {
        val debtId = UUID.randomUUID()

        Debts.insert {
            it[id] = debtId
            it[Debts.userId] = userId
            it[Debts.type] = type
            it[Debts.personName] = personName
            it[Debts.personContact] = personContact
            it[Debts.amount] = amount
            it[remainingAmount] = amount
            it[Debts.interestRate] = interestRate
            it[Debts.description] = description
            it[Debts.dueDate] = dueDate
            it[status] = "active"
            it[Debts.startDate] = startDate
            it[createdAt] = Instant.now()
            it[updatedAt] = Instant.now()
        }

        findById(debtId)!!
    }

    override suspend fun update(
        id: UUID,
        personName: String?,
        personContact: String?,
        interestRate: BigDecimal?,
        description: String?,
        dueDate: LocalDate?,
        status: String?
    ): Debt? = newSuspendedTransaction {
        val updated = Debts.update({ Debts.id eq id }) {
            personName?.let { name -> it[Debts.personName] = name }
            personContact?.let { contact -> it[Debts.personContact] = contact }
            interestRate?.let { rate -> it[Debts.interestRate] = rate }
            description?.let { desc -> it[Debts.description] = desc }
            dueDate?.let { date -> it[Debts.dueDate] = date }
            status?.let { stat -> it[Debts.status] = stat }
            it[updatedAt] = Instant.now()
        }

        if (updated > 0) findById(id) else null
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        Debts.deleteWhere { Debts.id eq id } > 0
    }

    private fun ResultRow.toDebt() = Debt(
        id = this[Debts.id],
        userId = this[Debts.userId],
        type = this[Debts.type],
        personName = this[Debts.personName],
        personContact = this[Debts.personContact],
        amount = this[Debts.amount],
        remainingAmount = this[Debts.remainingAmount],
        interestRate = this[Debts.interestRate],
        description = this[Debts.description],
        dueDate = this[Debts.dueDate],
        status = this[Debts.status],
        startDate = this[Debts.startDate],
        createdAt = this[Debts.createdAt],
        updatedAt = this[Debts.updatedAt]
    )

    private fun ResultRow.toDebtPayment() = DebtPayment(
        id = this[DebtPayments.id],
        debtId = this[DebtPayments.debtId],
        amount = this[DebtPayments.amount],
        paymentDate = this[DebtPayments.paymentDate],
        notes = this[DebtPayments.notes],
        createdAt = this[DebtPayments.createdAt]
    )
}

class DebtPaymentRepository : IDebtPaymentRepository {

    override suspend fun findById(id: UUID): DebtPayment? = newSuspendedTransaction {
        DebtPayments.select { DebtPayments.id eq id }
            .singleOrNull()
            ?.toDebtPayment()
    }

    override suspend fun findByDebtId(debtId: UUID): List<DebtPayment> = newSuspendedTransaction {
        DebtPayments.select { DebtPayments.debtId eq debtId }
            .orderBy(DebtPayments.paymentDate to SortOrder.DESC)
            .map { it.toDebtPayment() }
    }

    override suspend fun create(
        debtId: UUID,
        amount: BigDecimal,
        paymentDate: LocalDate,
        notes: String?
    ): DebtPayment = newSuspendedTransaction {
        val paymentId = UUID.randomUUID()

        DebtPayments.insert {
            it[id] = paymentId
            it[DebtPayments.debtId] = debtId
            it[DebtPayments.amount] = amount
            it[DebtPayments.paymentDate] = paymentDate
            it[DebtPayments.notes] = notes
            it[createdAt] = Instant.now()
        }

        findById(paymentId)!!
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        DebtPayments.deleteWhere { DebtPayments.id eq id } > 0
    }

    private fun ResultRow.toDebtPayment() = DebtPayment(
        id = this[DebtPayments.id],
        debtId = this[DebtPayments.debtId],
        amount = this[DebtPayments.amount],
        paymentDate = this[DebtPayments.paymentDate],
        notes = this[DebtPayments.notes],
        createdAt = this[DebtPayments.createdAt]
    )
}

