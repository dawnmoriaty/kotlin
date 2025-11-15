package com.financial.domain.services

import com.financial.dtos.request.CreateCategoryRequest
import com.financial.dtos.request.UpdateCategoryRequest
import com.financial.dtos.response.CategoryResponse
import java.util.*

interface ICategoryService {
    /**
     * Get all categories for user
     */
    suspend fun getAllCategories(userId: UUID): List<CategoryResponse>

    /**
     * Get categories by type (income/expense)
     */
    suspend fun getCategoriesByType(userId: UUID, type: String): List<CategoryResponse>

    /**
     * Get category by ID
     */
    suspend fun getCategoryById(id: UUID, userId: UUID): CategoryResponse

    /**
     * Create new category
     */
    suspend fun createCategory(userId: UUID, request: CreateCategoryRequest): CategoryResponse

    /**
     * Update category
     */
    suspend fun updateCategory(id: UUID, userId: UUID, request: UpdateCategoryRequest): CategoryResponse

    /**
     * Delete category
     */
    suspend fun deleteCategory(id: UUID, userId: UUID): Boolean

    /**
     * Create default categories for new user
     */
    suspend fun createDefaultCategories(userId: UUID): List<CategoryResponse>
}

