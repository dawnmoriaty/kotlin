package com.financial.domain.services

import com.financial.dtos.request.CreateRecurringTransactionRequest
import com.financial.dtos.request.UpdateRecurringTransactionRequest
import com.financial.dtos.response.RecurringTransactionResponse
import com.financial.dtos.response.RecurringTransactionSummaryResponse
import java.util.*

interface IRecurringTransactionService {
    suspend fun getRecurringTransactions(userId: UUID): List<RecurringTransactionResponse>
    suspend fun getRecurringTransactionById(userId: UUID, id: UUID): RecurringTransactionResponse?
    suspend fun getDueRecurringTransactions(userId: UUID): List<RecurringTransactionResponse>
    suspend fun getRecurringSummary(userId: UUID): RecurringTransactionSummaryResponse
    suspend fun createRecurringTransaction(userId: UUID, request: CreateRecurringTransactionRequest): RecurringTransactionResponse
    suspend fun updateRecurringTransaction(userId: UUID, id: UUID, request: UpdateRecurringTransactionRequest): RecurringTransactionResponse?
    suspend fun deleteRecurringTransaction(userId: UUID, id: UUID): Boolean
    suspend fun executeRecurringTransaction(userId: UUID, id: UUID): Boolean
    suspend fun processRecurringTransactions()
}

