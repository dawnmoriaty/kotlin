package com.financial.domain.service.impl

import com.financial.data.repository.ICategoryRepository
import com.financial.domain.exceptions.AuthException
import com.financial.domain.services.ICategoryService
import com.financial.dtos.request.CreateCategoryRequest
import com.financial.dtos.request.UpdateCategoryRequest
import com.financial.dtos.response.CategoryResponse
import java.util.*

class CategoryService(
    private val categoryRepository: ICategoryRepository
) : ICategoryService {

    override suspend fun getAllCategories(userId: UUID): List<CategoryResponse> {
        return categoryRepository.findByUserId(userId)
            .map { it.toCategoryResponse() }
    }

    override suspend fun getCategoriesByType(userId: UUID, type: String): List<CategoryResponse> {
        validateType(type)
        return categoryRepository.findByUserIdAndType(userId, type.lowercase())
            .map { it.toCategoryResponse() }
    }

    override suspend fun getCategoryById(id: UUID, userId: UUID): CategoryResponse {
        val category = categoryRepository.findById(id)
            ?: throw IllegalArgumentException("Category not found")

        if (category.userId != userId) {
            throw AuthException("You don't have permission to access this category")
        }

        return category.toCategoryResponse()
    }

    override suspend fun createCategory(userId: UUID, request: CreateCategoryRequest): CategoryResponse {
        // Validate type
        validateType(request.type)

        // Check if category name already exists for user
        if (categoryRepository.existsByNameAndUserId(request.name, userId)) {
            throw IllegalArgumentException("Category with name '${request.name}' already exists")
        }

        // Validate name length
        if (request.name.isBlank() || request.name.length > 100) {
            throw IllegalArgumentException("Category name must be between 1 and 100 characters")
        }

        val category = categoryRepository.create(
            name = request.name.trim(),
            type = request.type.lowercase(),
            icon = request.icon,
            userId = userId,
            isDefault = false
        )

        return category.toCategoryResponse()
    }

    override suspend fun updateCategory(
        id: UUID,
        userId: UUID,
        request: UpdateCategoryRequest
    ): CategoryResponse {
        val category = categoryRepository.findById(id)
            ?: throw IllegalArgumentException("Category not found")

        if (category.userId != userId) {
            throw AuthException("You don't have permission to update this category")
        }

        // Cannot update default categories
        if (category.isDefault) {
            throw IllegalArgumentException("Cannot update default categories")
        }

        // Check if new name already exists
        request.name?.let { newName ->
            if (newName.isBlank() || newName.length > 100) {
                throw IllegalArgumentException("Category name must be between 1 and 100 characters")
            }

            if (newName != category.name && categoryRepository.existsByNameAndUserId(newName, userId)) {
                throw IllegalArgumentException("Category with name '$newName' already exists")
            }
        }

        val updatedCategory = category.copy(
            name = request.name?.trim() ?: category.name,
            icon = request.icon ?: category.icon
        )

        categoryRepository.update(updatedCategory)

        return updatedCategory.toCategoryResponse()
    }

    override suspend fun deleteCategory(id: UUID, userId: UUID): Boolean {
        val category = categoryRepository.findById(id)
            ?: throw IllegalArgumentException("Category not found")

        if (category.userId != userId) {
            throw AuthException("You don't have permission to delete this category")
        }

        // Cannot delete default categories
        if (category.isDefault) {
            throw IllegalArgumentException("Cannot delete default categories")
        }

        return categoryRepository.delete(id, userId)
    }

    override suspend fun createDefaultCategories(userId: UUID): List<CategoryResponse> {
        val defaultCategories = listOf(
            // Income categories
            Triple("Salary", "income", "💰"),
            Triple("Business", "income", "💼"),
            Triple("Investment", "income", "📈"),
            Triple("Gift", "income", "🎁"),
            Triple("Other Income", "income", "💵"),

            // Expense categories
            Triple("Food & Dining", "expense", "🍔"),
            Triple("Transportation", "expense", "🚗"),
            Triple("Shopping", "expense", "🛍️"),
            Triple("Entertainment", "expense", "🎬"),
            Triple("Bills & Utilities", "expense", "💡"),
            Triple("Healthcare", "expense", "🏥"),
            Triple("Education", "expense", "📚"),
            Triple("Housing", "expense", "🏠"),
            Triple("Other Expense", "expense", "💳")
        )

        val createdCategories = mutableListOf<CategoryResponse>()

        for ((name, type, icon) in defaultCategories) {
            try {
                val category = categoryRepository.create(
                    name = name,
                    type = type,
                    icon = icon,
                    userId = userId,
                    isDefault = true
                )
                createdCategories.add(category.toCategoryResponse())
            } catch (e: Exception) {
                println("⚠️ Failed to create default category '$name': ${e.message}")
            }
        }

        return createdCategories
    }

    private fun validateType(type: String) {
        if (type.lowercase() !in listOf("income", "expense")) {
            throw IllegalArgumentException("Type must be either 'income' or 'expense'")
        }
    }

    private fun com.financial.data.model.Category.toCategoryResponse(): CategoryResponse {
        return CategoryResponse(
            id = id.toString(),
            name = name,
            type = type.name.lowercase(),
            icon = icon,
            userId = userId.toString(),
            isDefault = isDefault,
            createdAt = createdAt.toString()
        )
    }
}

