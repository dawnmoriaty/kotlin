package com.financial.data.repository

import com.financial.data.model.Category
import java.util.*

interface ICategoryRepository {
    suspend fun findById(id: UUID): Category?
    suspend fun findByUserId(userId: UUID): List<Category>
    suspend fun findByUserIdAndType(userId: UUID, type: String): List<Category>
    suspend fun create(name: String, type: String, icon: String?, userId: UUID, isDefault: Boolean = false): Category
    suspend fun update(category: Category): Category
    suspend fun delete(id: UUID, userId: UUID): Boolean
    suspend fun existsByNameAndUserId(name: String, userId: UUID): Boolean
}

