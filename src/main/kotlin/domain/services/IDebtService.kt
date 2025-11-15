package com.financial.domain.services

import com.financial.dtos.request.CreateDebtPaymentRequest
import com.financial.dtos.request.CreateDebtRequest
import com.financial.dtos.request.UpdateDebtRequest
import com.financial.dtos.response.*
import java.util.*

interface IDebtService {
    suspend fun getDebts(userId: UUID, type: String?): List<DebtResponse>
    suspend fun getDebtById(userId: UUID, debtId: UUID): DebtResponse?
    suspend fun getDebtDetail(userId: UUID, debtId: UUID): DebtDetailResponse?
    suspend fun getDebtSummary(userId: UUID): DebtSummaryResponse
    suspend fun getOverdueDebts(userId: UUID): List<DebtResponse>
    suspend fun createDebt(userId: UUID, request: CreateDebtRequest): DebtResponse
    suspend fun updateDebt(userId: UUID, debtId: UUID, request: UpdateDebtRequest): DebtResponse?
    suspend fun deleteDebt(userId: UUID, debtId: UUID): Boolean
    suspend fun addPayment(userId: UUID, debtId: UUID, request: CreateDebtPaymentRequest): DebtPaymentResponse
    suspend fun getPayments(userId: UUID, debtId: UUID): List<DebtPaymentResponse>
    suspend fun deletePayment(userId: UUID, debtId: UUID, paymentId: UUID): Boolean
}

