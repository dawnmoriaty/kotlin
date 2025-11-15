package com.financial.data.repository.impl

import com.financial.data.database.tables.Budgets
import com.financial.data.database.tables.Categories
import com.financial.data.database.tables.Transactions
import com.financial.data.model.Budget
import com.financial.data.model.BudgetSpending
import com.financial.data.repository.IBudgetRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.util.*

class BudgetRepository : IBudgetRepository {

    override suspend fun findById(id: UUID): Budget? = newSuspendedTransaction {
        Budgets.select { Budgets.id eq id }
            .singleOrNull()
            ?.toBudget()
    }

    override suspend fun findByUserId(userId: UUID): List<Budget> = newSuspendedTransaction {
        Budgets.select { Budgets.userId eq userId }
            .orderBy(Budgets.createdAt to SortOrder.DESC)
            .map { it.toBudget() }
    }

    override suspend fun findByUserIdAndPeriod(userId: UUID, period: String): List<Budget> = newSuspendedTransaction {
        Budgets.select {
            (Budgets.userId eq userId) and (Budgets.period eq period)
        }
            .orderBy(Budgets.createdAt to SortOrder.DESC)
            .map { it.toBudget() }
    }

    override suspend fun findActiveByUserId(userId: UUID): List<Budget> = newSuspendedTransaction {
        Budgets.select {
            (Budgets.userId eq userId) and (Budgets.isActive eq true)
        }
            .orderBy(Budgets.createdAt to SortOrder.DESC)
            .map { it.toBudget() }
    }

    override suspend fun getBudgetSpending(userId: UUID): List<BudgetSpending> = newSuspendedTransaction {
        val budgets = Budgets
            .innerJoin(Categories, { categoryId }, { Categories.id })
            .select {
                (Budgets.userId eq userId) and (Budgets.isActive eq true)
            }
            .map {
                val budget = it.toBudget()
                val categoryName = it[Categories.name]
                budget to categoryName
            }

        budgets.map { (budget, categoryName) ->
            val spentAmount = calculateSpentAmount(userId, budget)
            val remainingAmount = budget.amount - spentAmount
            val spentPercentage = if (budget.amount > BigDecimal.ZERO) {
                (spentAmount.divide(budget.amount, 4, RoundingMode.HALF_UP) * BigDecimal(100))
                    .setScale(2, RoundingMode.HALF_UP)
            } else BigDecimal.ZERO

            val isExceeded = spentAmount >= budget.amount
            val shouldAlert = spentPercentage >= budget.alertPercentage

            BudgetSpending(
                budget = budget,
                categoryName = categoryName,
                spentAmount = spentAmount,
                remainingAmount = remainingAmount,
                spentPercentage = spentPercentage,
                isExceeded = isExceeded,
                shouldAlert = shouldAlert
            )
        }
    }

    override suspend fun getBudgetSpendingByPeriod(userId: UUID, period: String): List<BudgetSpending> = newSuspendedTransaction {
        val budgets = Budgets
            .innerJoin(Categories, { categoryId }, { Categories.id })
            .select {
                (Budgets.userId eq userId) and
                (Budgets.isActive eq true) and
                (Budgets.period eq period)
            }
            .map {
                val budget = it.toBudget()
                val categoryName = it[Categories.name]
                budget to categoryName
            }

        budgets.map { (budget, categoryName) ->
            val spentAmount = calculateSpentAmount(userId, budget)
            val remainingAmount = budget.amount - spentAmount
            val spentPercentage = if (budget.amount > BigDecimal.ZERO) {
                (spentAmount.divide(budget.amount, 4, RoundingMode.HALF_UP) * BigDecimal(100))
                    .setScale(2, RoundingMode.HALF_UP)
            } else BigDecimal.ZERO

            BudgetSpending(
                budget = budget,
                categoryName = categoryName,
                spentAmount = spentAmount,
                remainingAmount = remainingAmount,
                spentPercentage = spentPercentage,
                isExceeded = spentAmount >= budget.amount,
                shouldAlert = spentPercentage >= budget.alertPercentage
            )
        }
    }

    private suspend fun calculateSpentAmount(userId: UUID, budget: Budget): BigDecimal = newSuspendedTransaction {
        val query = Transactions.select {
            (Transactions.userId eq userId) and
            (Transactions.categoryId eq budget.categoryId) and
            (Transactions.transactionDate greaterEq budget.startDate)
        }

        val queryWithEndDate = if (budget.endDate != null) {
            query.andWhere { Transactions.transactionDate lessEq budget.endDate }
        } else query

        queryWithEndDate
            .sumOf { it[Transactions.amount] }
    }

    override suspend fun create(
        userId: UUID,
        categoryId: UUID,
        amount: BigDecimal,
        period: String,
        startDate: LocalDate,
        endDate: LocalDate?,
        alertPercentage: BigDecimal
    ): Budget = newSuspendedTransaction {
        val budgetId = UUID.randomUUID()

        Budgets.insert {
            it[id] = budgetId
            it[Budgets.userId] = userId
            it[Budgets.categoryId] = categoryId
            it[Budgets.amount] = amount
            it[Budgets.period] = period
            it[Budgets.startDate] = startDate
            it[Budgets.endDate] = endDate
            it[Budgets.alertPercentage] = alertPercentage
            it[isActive] = true
            it[createdAt] = Instant.now()
            it[updatedAt] = Instant.now()
        }

        findById(budgetId)!!
    }

    override suspend fun update(
        id: UUID,
        amount: BigDecimal?,
        period: String?,
        endDate: LocalDate?,
        isActive: Boolean?,
        alertPercentage: BigDecimal?
    ): Budget? = newSuspendedTransaction {
        val updated = Budgets.update({ Budgets.id eq id }) {
            amount?.let { amt -> it[Budgets.amount] = amt }
            period?.let { per -> it[Budgets.period] = per }
            endDate?.let { date -> it[Budgets.endDate] = date }
            isActive?.let { active -> it[Budgets.isActive] = active }
            alertPercentage?.let { alert -> it[Budgets.alertPercentage] = alert }
            it[updatedAt] = Instant.now()
        }

        if (updated > 0) findById(id) else null
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        Budgets.deleteWhere { Budgets.id eq id } > 0
    }

    private fun ResultRow.toBudget() = Budget(
        id = this[Budgets.id],
        userId = this[Budgets.userId],
        categoryId = this[Budgets.categoryId],
        amount = this[Budgets.amount],
        period = this[Budgets.period],
        startDate = this[Budgets.startDate],
        endDate = this[Budgets.endDate],
        isActive = this[Budgets.isActive],
        alertPercentage = this[Budgets.alertPercentage],
        createdAt = this[Budgets.createdAt],
        updatedAt = this[Budgets.updatedAt]
    )
}

