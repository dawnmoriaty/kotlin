package com.financial.data.repository.impl

import com.financial.data.database.tables.Categories
import com.financial.data.database.tables.Transactions
import com.financial.data.model.Transaction
import com.financial.data.repository.ITransactionRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class TransactionRepository : ITransactionRepository {

    override suspend fun findById(id: UUID): Transaction? = newSuspendedTransaction {
        Transactions.selectAll().where { Transactions.id eq id }
            .map { rowToTransaction(it) }
            .singleOrNull()
    }

    override suspend fun findByUserId(
        userId: UUID,
        categoryId: UUID?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        limit: Int,
        offset: Int
    ): List<Transaction> = newSuspendedTransaction {
        var query = Transactions.selectAll().where { Transactions.userId eq userId }

        // Apply filters
        categoryId?.let {
            query = query.andWhere { Transactions.categoryId eq it }
        }

        startDate?.let {
            query = query.andWhere { Transactions.transactionDate greaterEq it }
        }

        endDate?.let {
            query = query.andWhere { Transactions.transactionDate lessEq it }
        }

        query
            .orderBy(Transactions.transactionDate to SortOrder.DESC, Transactions.createdAt to SortOrder.DESC)
            .limit(limit, offset.toLong())
            .map { rowToTransaction(it) }
    }

    override suspend fun create(
        description: String,
        amount: BigDecimal,
        transactionDate: LocalDate,
        categoryId: UUID,
        userId: UUID
    ): Transaction = newSuspendedTransaction {
        val now = LocalDateTime.now()
        val id = UUID.randomUUID()

        Transactions.insert {
            it[Transactions.id] = id
            it[Transactions.description] = description
            it[Transactions.amount] = amount
            it[Transactions.transactionDate] = transactionDate
            it[Transactions.categoryId] = categoryId
            it[Transactions.userId] = userId
            it[createdAt] = now
            it[updatedAt] = now
        }

        Transaction(
            id = id,
            description = description,
            amount = amount,
            transactionDate = transactionDate,
            categoryId = categoryId,
            userId = userId,
            createdAt = now,
            updatedAt = now
        )
    }

    override suspend fun update(transaction: Transaction): Transaction = newSuspendedTransaction {
        val now = LocalDateTime.now()

        Transactions.update({ Transactions.id eq transaction.id }) {
            it[description] = transaction.description
            it[amount] = transaction.amount
            it[transactionDate] = transaction.transactionDate
            it[categoryId] = transaction.categoryId
            it[updatedAt] = now
        }

        transaction.copy(updatedAt = now)
    }

    override suspend fun delete(id: UUID, userId: UUID): Boolean = newSuspendedTransaction {
        Transactions.deleteWhere {
            (Transactions.id eq id) and (Transactions.userId eq userId)
        } > 0
    }

    override suspend fun countByUserId(userId: UUID): Long = newSuspendedTransaction {
        Transactions.selectAll().where { Transactions.userId eq userId }.count()
    }

    override suspend fun getTotalIncomeByUserId(
        userId: UUID,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): BigDecimal = newSuspendedTransaction {
        val join = Transactions.innerJoin(Categories)

        var query = join.select(Transactions.amount.sum())
            .where { (Transactions.userId eq userId) and (Categories.type eq "income") }

        startDate?.let {
            query = query.andWhere { Transactions.transactionDate greaterEq it }
        }

        endDate?.let {
            query = query.andWhere { Transactions.transactionDate lessEq it }
        }

        query.firstOrNull()?.get(Transactions.amount.sum()) ?: BigDecimal.ZERO
    }

    override suspend fun getTotalExpenseByUserId(
        userId: UUID,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): BigDecimal = newSuspendedTransaction {
        val join = Transactions.innerJoin(Categories)

        var query = join.select(Transactions.amount.sum())
            .where { (Transactions.userId eq userId) and (Categories.type eq "expense") }

        startDate?.let {
            query = query.andWhere { Transactions.transactionDate greaterEq it }
        }

        endDate?.let {
            query = query.andWhere { Transactions.transactionDate lessEq it }
        }

        query.firstOrNull()?.get(Transactions.amount.sum()) ?: BigDecimal.ZERO
    }

    override suspend fun getTransactionCountByType(
        userId: UUID,
        type: String,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): Long = newSuspendedTransaction {
        val join = Transactions.innerJoin(Categories)

        var query = join.selectAll()
            .where { (Transactions.userId eq userId) and (Categories.type eq type) }

        startDate?.let {
            query = query.andWhere { Transactions.transactionDate greaterEq it }
        }

        endDate?.let {
            query = query.andWhere { Transactions.transactionDate lessEq it }
        }

        query.count()
    }

    private fun rowToTransaction(row: ResultRow): Transaction {
        return Transaction(
            id = row[Transactions.id].value,
            description = row[Transactions.description],
            amount = row[Transactions.amount],
            transactionDate = row[Transactions.transactionDate],
            categoryId = row[Transactions.categoryId],
            userId = row[Transactions.userId],
            createdAt = row[Transactions.createdAt],
            updatedAt = row[Transactions.updatedAt]
        )
    }
}

