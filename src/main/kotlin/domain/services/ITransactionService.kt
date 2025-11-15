package com.financial.domain.services

import com.financial.dtos.request.CreateTransactionRequest
import com.financial.dtos.request.UpdateTransactionRequest
import com.financial.dtos.response.DashboardResponse
import com.financial.dtos.response.TransactionResponse
import com.financial.dtos.response.TransactionStatistics
import com.financial.dtos.response.CategoryStatistics
import java.util.*

interface ITransactionService {
    /**
     * Get dashboard overview with all statistics
     */
    suspend fun getDashboard(userId: UUID): DashboardResponse

    /**
     * Get all transactions with filters
     */
    suspend fun getTransactions(
        userId: UUID,
        categoryId: UUID? = null,
        startDate: String? = null,
        endDate: String? = null,
        limit: Int = 50,
        offset: Int = 0
    ): List<TransactionResponse>

    /**
     * Get transaction by ID
     */
    suspend fun getTransactionById(id: UUID, userId: UUID): TransactionResponse

    /**
     * Create transaction
     */
    suspend fun createTransaction(userId: UUID, request: CreateTransactionRequest): TransactionResponse

    /**
     * Update transaction
     */
    suspend fun updateTransaction(id: UUID, userId: UUID, request: UpdateTransactionRequest): TransactionResponse

    /**
     * Delete transaction
     */
    suspend fun deleteTransaction(id: UUID, userId: UUID): Boolean

    /**
     * Get statistics (total income, expense, balance)
     */
    suspend fun getStatistics(userId: UUID, startDate: String?, endDate: String?): TransactionStatistics

    /**
     * Get statistics by category
     */
    suspend fun getCategoryStatistics(userId: UUID, startDate: String?, endDate: String?): List<CategoryStatistics>
}


