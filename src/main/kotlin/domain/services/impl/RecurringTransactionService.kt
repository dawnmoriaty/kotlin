package com.financial.domain.service.impl

import com.financial.data.repository.ICategoryRepository
import com.financial.data.repository.IRecurringTransactionRepository
import com.financial.data.repository.ITransactionRepository
import com.financial.domain.exceptions.NotFoundException
import com.financial.domain.services.IRecurringTransactionService
import com.financial.dtos.request.CreateRecurringTransactionRequest
import com.financial.dtos.request.UpdateRecurringTransactionRequest
import com.financial.dtos.response.RecurringTransactionResponse
import com.financial.dtos.response.RecurringTransactionSummaryResponse
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

class RecurringTransactionService(
    private val recurringTransactionRepository: IRecurringTransactionRepository,
    private val categoryRepository: ICategoryRepository,
    private val transactionRepository: ITransactionRepository
) : IRecurringTransactionService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun getRecurringTransactions(userId: UUID): List<RecurringTransactionResponse> {
        val recurringTransactions = recurringTransactionRepository.findByUserId(userId)
        return recurringTransactions.map { toResponse(it) }
    }

    override suspend fun getRecurringTransactionById(userId: UUID, id: UUID): RecurringTransactionResponse? {
        val recurring = recurringTransactionRepository.findById(id) ?: return null
        if (recurring.userId != userId) return null
        return toResponse(recurring)
    }

    override suspend fun getDueRecurringTransactions(userId: UUID): List<RecurringTransactionResponse> {
        val dueTransactions = recurringTransactionRepository.findDueByUserId(userId, LocalDate.now())
        return dueTransactions.map { toResponse(it) }
    }

    override suspend fun getRecurringSummary(userId: UUID): RecurringTransactionSummaryResponse {
        val allRecurring = recurringTransactionRepository.findByUserId(userId)
        val active = allRecurring.filter { it.isActive }
        val inactive = allRecurring.filter { !it.isActive }

        // Calculate monthly total (convert all to monthly equivalent)
        val monthlyTotal = active.sumOf { recurring ->
            when (recurring.frequency) {
                "daily" -> recurring.amount * BigDecimal(30)
                "weekly" -> recurring.amount * BigDecimal(4)
                "monthly" -> recurring.amount
                "yearly" -> recurring.amount / BigDecimal(12)
                else -> BigDecimal.ZERO
            }
        }

        val yearlyTotal = monthlyTotal * BigDecimal(12)
        val nextDueDate = active.minOfOrNull { it.nextOccurrence }

        return RecurringTransactionSummaryResponse(
            totalActive = active.size,
            totalInactive = inactive.size,
            monthlyTotal = monthlyTotal.toString(),
            yearlyTotal = yearlyTotal.toString(),
            nextDueDate = nextDueDate?.toString(),
            transactions = active.map { toResponse(it) }
        )
    }

    override suspend fun createRecurringTransaction(
        userId: UUID,
        request: CreateRecurringTransactionRequest
    ): RecurringTransactionResponse {
        // Validate category
        val category = categoryRepository.findById(UUID.fromString(request.categoryId))
            ?: throw NotFoundException("Category not found")

        if (category.userId != userId) {
            throw NotFoundException("Category not found")
        }

        // Validate frequency
        val validFrequencies = listOf("daily", "weekly", "monthly", "yearly")
        if (request.frequency !in validFrequencies) {
            throw IllegalArgumentException("Invalid frequency. Must be one of: ${validFrequencies.joinToString()}")
        }

        val recurring = recurringTransactionRepository.create(
            userId = userId,
            categoryId = UUID.fromString(request.categoryId),
            description = request.description,
            amount = BigDecimal(request.amount),
            frequency = request.frequency,
            startDate = LocalDate.parse(request.startDate),
            endDate = request.endDate?.let { LocalDate.parse(it) },
            autoCreate = request.autoCreate,
            dayOfMonth = request.dayOfMonth,
            dayOfWeek = request.dayOfWeek
        )

        return toResponse(recurring)
    }

    override suspend fun updateRecurringTransaction(
        userId: UUID,
        id: UUID,
        request: UpdateRecurringTransactionRequest
    ): RecurringTransactionResponse? {
        val existing = recurringTransactionRepository.findById(id) ?: return null
        if (existing.userId != userId) return null

        val updated = recurringTransactionRepository.update(
            id = id,
            description = request.description,
            amount = request.amount?.let { BigDecimal(it) },
            frequency = request.frequency,
            endDate = request.endDate?.let { LocalDate.parse(it) },
            isActive = request.isActive,
            autoCreate = request.autoCreate,
            dayOfMonth = request.dayOfMonth,
            dayOfWeek = request.dayOfWeek
        ) ?: return null

        return toResponse(updated)
    }

    override suspend fun deleteRecurringTransaction(userId: UUID, id: UUID): Boolean {
        val recurring = recurringTransactionRepository.findById(id) ?: return false
        if (recurring.userId != userId) return false
        return recurringTransactionRepository.delete(id)
    }

    override suspend fun executeRecurringTransaction(userId: UUID, id: UUID): Boolean {
        val recurring = recurringTransactionRepository.findById(id) ?: return false
        if (recurring.userId != userId) return false

        // Create transaction
        transactionRepository.create(
            userId = userId,
            categoryId = recurring.categoryId,
            amount = recurring.amount,
            description = recurring.description,
            transactionDate = LocalDate.now()
        )

        // Update next occurrence
        val nextOccurrence = calculateNextOccurrence(recurring)
        recurringTransactionRepository.updateNextOccurrence(id, nextOccurrence)

        logger.info("✅ Executed recurring transaction: ${recurring.description} for user: $userId")
        return true
    }

    override suspend fun processRecurringTransactions() {
        val today = LocalDate.now()
        val dueTransactions = recurringTransactionRepository.findDueTransactions(today)

        logger.info("🔄 Processing ${dueTransactions.size} due recurring transactions...")

        dueTransactions.forEach { recurring ->
            try {
                if (recurring.autoCreate) {
                    // Auto create transaction
                    transactionRepository.create(
                        userId = recurring.userId,
                        categoryId = recurring.categoryId,
                        amount = recurring.amount,
                        description = recurring.description,
                        transactionDate = today
                    )

                    logger.info("✅ Auto-created transaction: ${recurring.description}")
                }

                // Update next occurrence
                val nextOccurrence = calculateNextOccurrence(recurring)

                // Check if should deactivate (end date reached)
                if (recurring.endDate != null && nextOccurrence.isAfter(recurring.endDate)) {
                    recurringTransactionRepository.update(
                        id = recurring.id,
                        description = null,
                        amount = null,
                        frequency = null,
                        endDate = null,
                        isActive = false,
                        autoCreate = null,
                        dayOfMonth = null,
                        dayOfWeek = null
                    )
                    logger.info("🔚 Deactivated recurring transaction: ${recurring.description}")
                } else {
                    recurringTransactionRepository.updateNextOccurrence(recurring.id, nextOccurrence)
                }

            } catch (e: Exception) {
                logger.error("❌ Error processing recurring transaction ${recurring.id}: ${e.message}", e)
            }
        }
    }

    private fun calculateNextOccurrence(recurring: com.financial.data.model.RecurringTransaction): LocalDate {
        var next = recurring.nextOccurrence

        return when (recurring.frequency) {
            "daily" -> next.plusDays(1)
            "weekly" -> next.plusWeeks(1)
            "monthly" -> {
                val targetDay = recurring.dayOfMonth ?: next.dayOfMonth
                next = next.plusMonths(1)
                next.withDayOfMonth(minOf(targetDay, next.lengthOfMonth()))
            }
            "yearly" -> next.plusYears(1)
            else -> next.plusDays(1)
        }
    }

    private suspend fun toResponse(recurring: com.financial.data.model.RecurringTransaction): RecurringTransactionResponse {
        val category = categoryRepository.findById(recurring.categoryId)
        return RecurringTransactionResponse(
            id = recurring.id.toString(),
            categoryId = recurring.categoryId.toString(),
            categoryName = category?.name ?: "Unknown",
            description = recurring.description,
            amount = recurring.amount.toString(),
            frequency = recurring.frequency,
            startDate = recurring.startDate.toString(),
            endDate = recurring.endDate?.toString(),
            nextOccurrence = recurring.nextOccurrence.toString(),
            isActive = recurring.isActive,
            autoCreate = recurring.autoCreate,
            dayOfMonth = recurring.dayOfMonth,
            dayOfWeek = recurring.dayOfWeek,
            createdAt = recurring.createdAt.toString(),
            updatedAt = recurring.updatedAt.toString()
        )
    }
}

