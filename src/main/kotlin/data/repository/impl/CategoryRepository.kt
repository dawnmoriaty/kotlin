package com.financial.data.repository.impl

import com.financial.data.database.tables.Categories
import com.financial.data.model.Category
import com.financial.data.repository.ICategoryRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime
import java.util.*

class CategoryRepository : ICategoryRepository {

    override suspend fun findById(id: UUID): Category? = newSuspendedTransaction {
        Categories.selectAll().where { Categories.id eq id }
            .map { rowToCategory(it) }
            .singleOrNull()
    }

    override suspend fun findByUserId(userId: UUID): List<Category> = newSuspendedTransaction {
        Categories.selectAll().where { Categories.userId eq userId }
            .orderBy(Categories.createdAt to SortOrder.DESC)
            .map { rowToCategory(it) }
    }

    override suspend fun findByUserIdAndType(userId: UUID, type: String): List<Category> = newSuspendedTransaction {
        Categories.selectAll().where {
            (Categories.userId eq userId) and (Categories.type eq type)
        }
            .orderBy(Categories.createdAt to SortOrder.DESC)
            .map { rowToCategory(it) }
    }

    override suspend fun create(
        name: String,
        type: String,
        icon: String?,
        userId: UUID,
        isDefault: Boolean
    ): Category = newSuspendedTransaction {
        val now = LocalDateTime.now()
        val id = UUID.randomUUID()

        Categories.insert {
            it[Categories.id] = id
            it[Categories.name] = name
            it[Categories.type] = type
            it[Categories.icon] = icon
            it[Categories.userId] = userId
            it[Categories.isDefault] = isDefault
            it[createdAt] = now
        }

        Category(
            id = id,
            name = name,
            type = com.financial.data.database.enums.TransactionType.valueOf(type.uppercase()),
            icon = icon,
            userId = userId,
            isDefault = isDefault,
            createdAt = now
        )
    }

    override suspend fun update(category: Category): Category = newSuspendedTransaction {
        Categories.update({ Categories.id eq category.id }) {
            it[name] = category.name
            it[icon] = category.icon
        }

        category
    }

    override suspend fun delete(id: UUID, userId: UUID): Boolean = newSuspendedTransaction {
        Categories.deleteWhere {
            (Categories.id eq id) and (Categories.userId eq userId)
        } > 0
    }

    override suspend fun existsByNameAndUserId(name: String, userId: UUID): Boolean = newSuspendedTransaction {
        Categories.selectAll().where {
            (Categories.name eq name) and (Categories.userId eq userId)
        }.count() > 0
    }

    private fun rowToCategory(row: ResultRow): Category {
        return Category(
            id = row[Categories.id].value,
            name = row[Categories.name],
            type = com.financial.data.database.enums.TransactionType.valueOf(row[Categories.type].uppercase()),
            icon = row[Categories.icon],
            userId = row[Categories.userId],
            isDefault = row[Categories.isDefault],
            createdAt = row[Categories.createdAt]
        )
    }
}

