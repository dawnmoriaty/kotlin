package com.financial.domain.service.impl

import com.financial.data.repository.IDebtPaymentRepository
import com.financial.data.repository.IDebtRepository
import com.financial.domain.exceptions.NotFoundException
import com.financial.domain.services.IDebtService
import com.financial.dtos.request.CreateDebtPaymentRequest
import com.financial.dtos.request.CreateDebtRequest
import com.financial.dtos.request.UpdateDebtRequest
import com.financial.dtos.response.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

class DebtService(
    private val debtRepository: IDebtRepository,
    private val debtPaymentRepository: IDebtPaymentRepository
) : IDebtService {

    override suspend fun getDebts(userId: UUID, type: String?): List<DebtResponse> {
        val debts = if (type != null) {
            debtRepository.findByUserIdAndType(userId, type)
        } else {
            debtRepository.findByUserId(userId)
        }

        return debts.map { debt ->
            val summary = debtRepository.getDebtSummary(debt.id)!!
            toResponse(summary)
        }
    }

    override suspend fun getDebtById(userId: UUID, debtId: UUID): DebtResponse? {
        val debt = debtRepository.findById(debtId) ?: return null
        if (debt.userId != userId) return null

        val summary = debtRepository.getDebtSummary(debtId) ?: return null
        return toResponse(summary)
    }

    override suspend fun getDebtDetail(userId: UUID, debtId: UUID): DebtDetailResponse? {
        val debt = debtRepository.findById(debtId) ?: return null
        if (debt.userId != userId) return null

        val summary = debtRepository.getDebtSummary(debtId) ?: return null
        val payments = debtPaymentRepository.findByDebtId(debtId)

        return DebtDetailResponse(
            debt = toResponse(summary),
            payments = payments.map { toPaymentResponse(it) },
            paymentCount = payments.size,
            lastPaymentDate = payments.maxOfOrNull { it.paymentDate }?.toString()
        )
    }

    override suspend fun getDebtSummary(userId: UUID): DebtSummaryResponse {
        val allDebts = debtRepository.getDebtSummaries(userId)

        val borrowed = allDebts.filter { it.debt.type == "borrowed" }
        val lent = allDebts.filter { it.debt.type == "lent" }

        val totalBorrowed = borrowed.sumOf { it.debt.amount }
        val totalLent = lent.sumOf { it.debt.amount }
        val totalBorrowedRemaining = borrowed.sumOf { it.debt.remainingAmount }
        val totalLentRemaining = lent.sumOf { it.debt.remainingAmount }
        val totalOverdue = allDebts.count { it.debt.status == "overdue" }

        return DebtSummaryResponse(
            totalBorrowed = totalBorrowed.toString(),
            totalLent = totalLent.toString(),
            totalBorrowedRemaining = totalBorrowedRemaining.toString(),
            totalLentRemaining = totalLentRemaining.toString(),
            totalOverdue = totalOverdue,
            borrowedDebts = borrowed.map { toResponse(it) },
            lentDebts = lent.map { toResponse(it) }
        )
    }

    override suspend fun getOverdueDebts(userId: UUID): List<DebtResponse> {
        val overdueDebts = debtRepository.findOverdueByUserId(userId)
        return overdueDebts.map { debt ->
            val summary = debtRepository.getDebtSummary(debt.id)!!
            toResponse(summary)
        }
    }

    override suspend fun createDebt(userId: UUID, request: CreateDebtRequest): DebtResponse {
        // Validate type
        val validTypes = listOf("borrowed", "lent")
        if (request.type !in validTypes) {
            throw IllegalArgumentException("Invalid type. Must be one of: ${validTypes.joinToString()}")
        }

        val debt = debtRepository.create(
            userId = userId,
            type = request.type,
            personName = request.personName,
            personContact = request.personContact,
            amount = BigDecimal(request.amount),
            interestRate = BigDecimal(request.interestRate ?: "0"),
            description = request.description,
            dueDate = request.dueDate?.let { LocalDate.parse(it) },
            startDate = request.startDate?.let { LocalDate.parse(it) } ?: LocalDate.now()
        )

        val summary = debtRepository.getDebtSummary(debt.id)!!
        return toResponse(summary)
    }

    override suspend fun updateDebt(userId: UUID, debtId: UUID, request: UpdateDebtRequest): DebtResponse? {
        val existing = debtRepository.findById(debtId) ?: return null
        if (existing.userId != userId) return null

        val updated = debtRepository.update(
            id = debtId,
            personName = request.personName,
            personContact = request.personContact,
            interestRate = request.interestRate?.let { BigDecimal(it) },
            description = request.description,
            dueDate = request.dueDate?.let { LocalDate.parse(it) },
            status = request.status
        ) ?: return null

        val summary = debtRepository.getDebtSummary(updated.id)!!
        return toResponse(summary)
    }

    override suspend fun deleteDebt(userId: UUID, debtId: UUID): Boolean {
        val debt = debtRepository.findById(debtId) ?: return false
        if (debt.userId != userId) return false
        return debtRepository.delete(debtId)
    }

    override suspend fun addPayment(
        userId: UUID,
        debtId: UUID,
        request: CreateDebtPaymentRequest
    ): DebtPaymentResponse {
        // Validate debt ownership
        val debt = debtRepository.findById(debtId)
            ?: throw NotFoundException("Debt not found")

        if (debt.userId != userId) {
            throw NotFoundException("Debt not found")
        }

        // Validate payment amount
        val paymentAmount = BigDecimal(request.amount)
        if (paymentAmount > debt.remainingAmount) {
            throw IllegalArgumentException("Payment amount exceeds remaining debt amount")
        }

        val payment = debtPaymentRepository.create(
            debtId = debtId,
            amount = paymentAmount,
            paymentDate = request.paymentDate?.let { LocalDate.parse(it) } ?: LocalDate.now(),
            notes = request.notes
        )

        return toPaymentResponse(payment)
    }

    override suspend fun getPayments(userId: UUID, debtId: UUID): List<DebtPaymentResponse> {
        // Verify debt ownership
        val debt = debtRepository.findById(debtId) ?: return emptyList()
        if (debt.userId != userId) return emptyList()

        val payments = debtPaymentRepository.findByDebtId(debtId)
        return payments.map { toPaymentResponse(it) }
    }

    override suspend fun deletePayment(userId: UUID, debtId: UUID, paymentId: UUID): Boolean {
        // Verify debt ownership
        val debt = debtRepository.findById(debtId) ?: return false
        if (debt.userId != userId) return false

        // Verify payment belongs to debt
        val payment = debtPaymentRepository.findById(paymentId) ?: return false
        if (payment.debtId != debtId) return false

        return debtPaymentRepository.delete(paymentId)
    }

    private fun toResponse(summary: com.financial.data.model.DebtSummary): DebtResponse {
        return DebtResponse(
            id = summary.debt.id.toString(),
            type = summary.debt.type,
            personName = summary.debt.personName,
            personContact = summary.debt.personContact,
            amount = summary.debt.amount.toString(),
            remainingAmount = summary.debt.remainingAmount.toString(),
            paidAmount = summary.paidAmount.toString(),
            paidPercentage = summary.paidPercentage.toString(),
            interestRate = summary.debt.interestRate.toString(),
            description = summary.debt.description,
            dueDate = summary.debt.dueDate?.toString(),
            status = summary.debt.status,
            startDate = summary.debt.startDate.toString(),
            daysOverdue = summary.daysOverdue,
            createdAt = summary.debt.createdAt.toString(),
            updatedAt = summary.debt.updatedAt.toString()
        )
    }

    private fun toPaymentResponse(payment: com.financial.data.model.DebtPayment): DebtPaymentResponse {
        return DebtPaymentResponse(
            id = payment.id.toString(),
            debtId = payment.debtId.toString(),
            amount = payment.amount.toString(),
            paymentDate = payment.paymentDate.toString(),
            notes = payment.notes,
            createdAt = payment.createdAt.toString()
        )
    }
}

