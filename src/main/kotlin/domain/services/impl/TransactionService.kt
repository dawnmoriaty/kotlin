package com.financial.domain.service.impl

import com.financial.data.repository.ICategoryRepository
import com.financial.data.repository.ITransactionRepository
import com.financial.domain.exceptions.AuthException
import com.financial.domain.services.ITransactionService
import com.financial.dtos.request.CreateTransactionRequest
import com.financial.dtos.request.UpdateTransactionRequest
import com.financial.dtos.response.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.util.*

class TransactionService(
    private val transactionRepository: ITransactionRepository,
    private val categoryRepository: ICategoryRepository
) : ITransactionService {

    override suspend fun getDashboard(userId: UUID): DashboardResponse {
        val today = LocalDate.now()
        val currentMonthStart = today.withDayOfMonth(1)
        val lastMonthStart = currentMonthStart.minusMonths(1)
        val lastMonthEnd = currentMonthStart.minusDays(1)
        val sixMonthsAgo = currentMonthStart.minusMonths(6)

        // Current month stats
        val currentMonthIncome = transactionRepository.getTotalIncomeByUserId(userId, currentMonthStart, today)
        val currentMonthExpense = transactionRepository.getTotalExpenseByUserId(userId, currentMonthStart, today)
        val balance = currentMonthIncome - currentMonthExpense

        // Last month stats (for comparison)
        val lastMonthIncome = transactionRepository.getTotalIncomeByUserId(userId, lastMonthStart, lastMonthEnd)
        val lastMonthExpense = transactionRepository.getTotalExpenseByUserId(userId, lastMonthStart, lastMonthEnd)

        // Calculate percentage changes
        val incomeChange = calculatePercentageChange(lastMonthIncome, currentMonthIncome)
        val expenseChange = calculatePercentageChange(lastMonthExpense, currentMonthExpense)

        // Overview
        val overview = OverviewStats(
            totalIncome = currentMonthIncome.toString(),
            totalExpense = currentMonthExpense.toString(),
            balance = balance.toString(),
            incomeVsLastMonth = incomeChange,
            expenseVsLastMonth = expenseChange
        )

        // Recent transactions (last 10)
        val recentTransactions = getTransactions(userId, null, null, null, 10, 0)

        // Top categories (current month)
        val allCategoryStats = getCategoryStatistics(userId, currentMonthStart.toString(), today.toString())
        val topExpenseCategories = allCategoryStats
            .filter { it.categoryType == "expense" }
            .take(5)
        val topIncomeCategories = allCategoryStats
            .filter { it.categoryType == "income" }
            .take(5)

        // Monthly trend (last 6 months)
        val monthlyTrend = mutableListOf<MonthlyStatistics>()
        var currentMonth = sixMonthsAgo
        while (currentMonth.isBefore(today) || currentMonth.isEqual(today)) {
            val monthStart = currentMonth.withDayOfMonth(1)
            val monthEnd = currentMonth.withDayOfMonth(currentMonth.lengthOfMonth())

            val monthIncome = transactionRepository.getTotalIncomeByUserId(userId, monthStart, monthEnd)
            val monthExpense = transactionRepository.getTotalExpenseByUserId(userId, monthStart, monthEnd)
            val monthBalance = monthIncome - monthExpense
            val monthTransactionCount = transactionRepository.getTransactionCountByType(userId, "income", monthStart, monthEnd) +
                    transactionRepository.getTransactionCountByType(userId, "expense", monthStart, monthEnd)

            monthlyTrend.add(
                MonthlyStatistics(
                    month = YearMonth.from(currentMonth).toString(),
                    totalIncome = monthIncome.toString(),
                    totalExpense = monthExpense.toString(),
                    balance = monthBalance.toString(),
                    transactionCount = monthTransactionCount.toInt()
                )
            )

            currentMonth = currentMonth.plusMonths(1)
        }

        // Quick stats
        val totalTransactions = transactionRepository.countByUserId(userId).toInt()
        val totalCategories = categoryRepository.findByUserId(userId).size

        // Average expense per day (current month)
        val daysInMonth = ChronoUnit.DAYS.between(currentMonthStart, today) + 1
        val avgExpensePerDay = if (daysInMonth > 0)
            currentMonthExpense.divide(BigDecimal(daysInMonth), 2, RoundingMode.HALF_UP)
        else BigDecimal.ZERO

        // Average income per month (last 6 months)
        val totalIncomeLastSixMonths = transactionRepository.getTotalIncomeByUserId(userId, sixMonthsAgo, today)
        val avgIncomePerMonth = totalIncomeLastSixMonths.divide(BigDecimal(6), 2, RoundingMode.HALF_UP)

        // Largest transactions
        val allTransactions = transactionRepository.findByUserId(userId, null, null, null, Int.MAX_VALUE, 0)
        val largestExpense = allTransactions
            .filter { tx ->
                val cat = categoryRepository.findById(tx.categoryId)
                cat?.type?.name?.lowercase() == "expense"
            }
            .maxByOrNull { it.amount }?.amount ?: BigDecimal.ZERO

        val largestIncome = allTransactions
            .filter { tx ->
                val cat = categoryRepository.findById(tx.categoryId)
                cat?.type?.name?.lowercase() == "income"
            }
            .maxByOrNull { it.amount }?.amount ?: BigDecimal.ZERO

        val quickStats = QuickStats(
            totalTransactions = totalTransactions,
            totalCategories = totalCategories,
            averageExpensePerDay = avgExpensePerDay.toString(),
            averageIncomePerMonth = avgIncomePerMonth.toString(),
            largestExpense = largestExpense.toString(),
            largestIncome = largestIncome.toString()
        )

        return DashboardResponse(
            overview = overview,
            recentTransactions = recentTransactions,
            topExpenseCategories = topExpenseCategories,
            topIncomeCategories = topIncomeCategories,
            monthlyTrend = monthlyTrend,
            quickStats = quickStats
        )
    }

    private fun calculatePercentageChange(oldValue: BigDecimal, newValue: BigDecimal): String {
        if (oldValue == BigDecimal.ZERO) {
            return if (newValue > BigDecimal.ZERO) "+100%" else "0%"
        }

        val change = ((newValue - oldValue).divide(oldValue, 4, RoundingMode.HALF_UP) * BigDecimal(100))
            .setScale(1, RoundingMode.HALF_UP)

        return if (change >= BigDecimal.ZERO) "+${change}%" else "${change}%"
    }

    override suspend fun getTransactions(
        userId: UUID,
        categoryId: UUID?,
        startDate: String?,
        endDate: String?,
        limit: Int,
        offset: Int
    ): List<TransactionResponse> {
        val start = startDate?.let { LocalDate.parse(it) }
        val end = endDate?.let { LocalDate.parse(it) }

        val transactions = transactionRepository.findByUserId(userId, categoryId, start, end, limit, offset)

        return transactions.map { transaction ->
            val category = categoryRepository.findById(transaction.categoryId)
                ?: throw IllegalArgumentException("Category not found")

            TransactionResponse(
                id = transaction.id.toString(),
                description = transaction.description,
                amount = transaction.amount.toString(),
                transactionDate = transaction.transactionDate.toString(),
                categoryId = category.id.toString(),
                categoryName = category.name,
                categoryType = category.type.name.lowercase(),
                categoryIcon = category.icon,
                userId = transaction.userId.toString(),
                createdAt = transaction.createdAt.toString(),
                updatedAt = transaction.updatedAt.toString()
            )
        }
    }

    override suspend fun getTransactionById(id: UUID, userId: UUID): TransactionResponse {
        val transaction = transactionRepository.findById(id)
            ?: throw IllegalArgumentException("Transaction not found")

        if (transaction.userId != userId) {
            throw AuthException("You don't have permission to access this transaction")
        }

        val category = categoryRepository.findById(transaction.categoryId)
            ?: throw IllegalArgumentException("Category not found")

        return TransactionResponse(
            id = transaction.id.toString(),
            description = transaction.description,
            amount = transaction.amount.toString(),
            transactionDate = transaction.transactionDate.toString(),
            categoryId = category.id.toString(),
            categoryName = category.name,
            categoryType = category.type.name.lowercase(),
            categoryIcon = category.icon,
            userId = transaction.userId.toString(),
            createdAt = transaction.createdAt.toString(),
            updatedAt = transaction.updatedAt.toString()
        )
    }

    override suspend fun createTransaction(
        userId: UUID,
        request: CreateTransactionRequest
    ): TransactionResponse {
        // Validate amount
        val amount = try {
            BigDecimal(request.amount)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid amount format")
        }

        if (amount <= BigDecimal.ZERO) {
            throw IllegalArgumentException("Amount must be greater than 0")
        }

        // Validate date
        val transactionDate = try {
            LocalDate.parse(request.transactionDate)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid date format. Use YYYY-MM-DD")
        }

        // Validate category exists and belongs to user
        val categoryId = UUID.fromString(request.categoryId)
        val category = categoryRepository.findById(categoryId)
            ?: throw IllegalArgumentException("Category not found")

        if (category.userId != userId) {
            throw AuthException("You don't have permission to use this category")
        }

        // Validate description
        if (request.description.isBlank() || request.description.length > 200) {
            throw IllegalArgumentException("Description must be between 1 and 200 characters")
        }

        // Create transaction
        val transaction = transactionRepository.create(
            description = request.description.trim(),
            amount = amount,
            transactionDate = transactionDate,
            categoryId = categoryId,
            userId = userId
        )

        return TransactionResponse(
            id = transaction.id.toString(),
            description = transaction.description,
            amount = transaction.amount.toString(),
            transactionDate = transaction.transactionDate.toString(),
            categoryId = category.id.toString(),
            categoryName = category.name,
            categoryType = category.type.name.lowercase(),
            categoryIcon = category.icon,
            userId = transaction.userId.toString(),
            createdAt = transaction.createdAt.toString(),
            updatedAt = transaction.updatedAt.toString()
        )
    }

    override suspend fun updateTransaction(
        id: UUID,
        userId: UUID,
        request: UpdateTransactionRequest
    ): TransactionResponse {
        val transaction = transactionRepository.findById(id)
            ?: throw IllegalArgumentException("Transaction not found")

        if (transaction.userId != userId) {
            throw AuthException("You don't have permission to update this transaction")
        }

        // Validate and parse new values
        val newAmount = request.amount?.let {
            try {
                val amount = BigDecimal(it)
                if (amount <= BigDecimal.ZERO) {
                    throw IllegalArgumentException("Amount must be greater than 0")
                }
                amount
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Invalid amount format")
            }
        } ?: transaction.amount

        val newDate = request.transactionDate?.let {
            try {
                LocalDate.parse(it)
            } catch (e: Exception) {
                throw IllegalArgumentException("Invalid date format. Use YYYY-MM-DD")
            }
        } ?: transaction.transactionDate

        val newCategoryId = request.categoryId?.let {
            val catId = UUID.fromString(it)
            val category = categoryRepository.findById(catId)
                ?: throw IllegalArgumentException("Category not found")

            if (category.userId != userId) {
                throw AuthException("You don't have permission to use this category")
            }
            catId
        } ?: transaction.categoryId

        val newDescription = request.description?.let {
            if (it.isBlank() || it.length > 200) {
                throw IllegalArgumentException("Description must be between 1 and 200 characters")
            }
            it.trim()
        } ?: transaction.description

        // Update transaction
        val updatedTransaction = transaction.copy(
            description = newDescription,
            amount = newAmount,
            transactionDate = newDate,
            categoryId = newCategoryId
        )

        transactionRepository.update(updatedTransaction)

        // Get category for response
        val category = categoryRepository.findById(newCategoryId)!!

        return TransactionResponse(
            id = updatedTransaction.id.toString(),
            description = updatedTransaction.description,
            amount = updatedTransaction.amount.toString(),
            transactionDate = updatedTransaction.transactionDate.toString(),
            categoryId = category.id.toString(),
            categoryName = category.name,
            categoryType = category.type.name.lowercase(),
            categoryIcon = category.icon,
            userId = updatedTransaction.userId.toString(),
            createdAt = updatedTransaction.createdAt.toString(),
            updatedAt = updatedTransaction.updatedAt.toString()
        )
    }

    override suspend fun deleteTransaction(id: UUID, userId: UUID): Boolean {
        val transaction = transactionRepository.findById(id)
            ?: throw IllegalArgumentException("Transaction not found")

        if (transaction.userId != userId) {
            throw AuthException("You don't have permission to delete this transaction")
        }

        return transactionRepository.delete(id, userId)
    }

    override suspend fun getStatistics(
        userId: UUID,
        startDate: String?,
        endDate: String?
    ): TransactionStatistics {
        val start = startDate?.let { LocalDate.parse(it) }
        val end = endDate?.let { LocalDate.parse(it) }

        val totalIncome = transactionRepository.getTotalIncomeByUserId(userId, start, end)
        val totalExpense = transactionRepository.getTotalExpenseByUserId(userId, start, end)
        val balance = totalIncome - totalExpense

        val incomeCount = transactionRepository.getTransactionCountByType(userId, "income", start, end)
        val expenseCount = transactionRepository.getTransactionCountByType(userId, "expense", start, end)

        return TransactionStatistics(
            totalIncome = totalIncome.toString(),
            totalExpense = totalExpense.toString(),
            balance = balance.toString(),
            transactionCount = (incomeCount + expenseCount).toInt(),
            incomeCount = incomeCount.toInt(),
            expenseCount = expenseCount.toInt()
        )
    }

    override suspend fun getCategoryStatistics(
        userId: UUID,
        startDate: String?,
        endDate: String?
    ): List<CategoryStatistics> {
        val start = startDate?.let { LocalDate.parse(it) }
        val end = endDate?.let { LocalDate.parse(it) }

        val transactions = transactionRepository.findByUserId(userId, null, start, end, Int.MAX_VALUE, 0)

        // Group by category
        val categoryMap = mutableMapOf<UUID, MutableList<com.financial.data.model.Transaction>>()
        transactions.forEach { transaction ->
            categoryMap.getOrPut(transaction.categoryId) { mutableListOf() }.add(transaction)
        }

        val totalIncome = transactionRepository.getTotalIncomeByUserId(userId, start, end)
        val totalExpense = transactionRepository.getTotalExpenseByUserId(userId, start, end)

        return categoryMap.map { (categoryId, txList) ->
            val category = categoryRepository.findById(categoryId)!!
            val totalAmount = txList.sumOf { it.amount }
            val percentage = if (category.type.name.lowercase() == "income") {
                if (totalIncome > BigDecimal.ZERO)
                    (totalAmount.divide(totalIncome, 4, RoundingMode.HALF_UP) * BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)
                else BigDecimal.ZERO
            } else {
                if (totalExpense > BigDecimal.ZERO)
                    (totalAmount.divide(totalExpense, 4, RoundingMode.HALF_UP) * BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)
                else BigDecimal.ZERO
            }

            CategoryStatistics(
                categoryId = categoryId.toString(),
                categoryName = category.name,
                categoryIcon = category.icon,
                categoryType = category.type.name.lowercase(),
                totalAmount = totalAmount.toString(),
                transactionCount = txList.size,
                percentage = percentage.toString()
            )
        }.sortedByDescending { it.totalAmount.toBigDecimal() }
    }
}
