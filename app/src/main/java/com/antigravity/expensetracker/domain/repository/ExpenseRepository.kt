package com.antigravity.expensetracker.domain.repository

import com.antigravity.expensetracker.data.local.CategorySum
import com.antigravity.expensetracker.data.model.Category
import com.antigravity.expensetracker.data.model.ExpenseEntity
import com.antigravity.expensetracker.data.model.PaymentMode
import com.antigravity.expensetracker.domain.model.DailySpending
import com.antigravity.expensetracker.domain.model.DashboardSummary
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate

interface ExpenseRepository {
    suspend fun insertExpense(expense: ExpenseEntity): Long
    suspend fun updateExpense(expense: ExpenseEntity)
    suspend fun deleteExpense(expense: ExpenseEntity)
    suspend fun deleteExpenseById(id: Long)
    suspend fun deleteAllExpenses()
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    fun getAllExpenses(): Flow<List<ExpenseEntity>>
    fun getRecentExpenses(limit: Int = 5): Flow<List<ExpenseEntity>>
    fun getExpensesBetween(startTime: Instant, endTime: Instant): Flow<List<ExpenseEntity>>
    fun getTotalSpentBetween(startTime: Instant, endTime: Instant): Flow<Double>
    fun getCategoryBreakdown(startTime: Instant, endTime: Instant): Flow<List<CategorySum>>
    fun searchExpenses(query: String, startTime: Instant, endTime: Instant): Flow<List<ExpenseEntity>>

    // Aggregations
    fun getDashboardSummary(): Flow<DashboardSummary>
    fun getExpensesByDate(date: LocalDate): Flow<List<ExpenseEntity>>
    fun getDailySpendingForMonth(yearMonth: LocalDate): Flow<Map<LocalDate, Double>>
}
