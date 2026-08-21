package com.antigravity.expensetracker.data.repository

import com.antigravity.expensetracker.data.local.CategorySum
import com.antigravity.expensetracker.data.local.ExpenseDao
import com.antigravity.expensetracker.data.model.Category
import com.antigravity.expensetracker.data.model.ExpenseEntity
import com.antigravity.expensetracker.domain.model.BudgetStatus
import com.antigravity.expensetracker.domain.model.DailySpending
import com.antigravity.expensetracker.domain.model.DashboardSummary
import com.antigravity.expensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

class ExpenseRepositoryImpl(
    private val dao: ExpenseDao
) : ExpenseRepository {

    private val zoneId = ZoneId.systemDefault()

    override suspend fun insertExpense(expense: ExpenseEntity): Long = dao.insertExpense(expense)

    override suspend fun updateExpense(expense: ExpenseEntity) = dao.updateExpense(expense)

    override suspend fun deleteExpense(expense: ExpenseEntity) = dao.deleteExpense(expense)

    override suspend fun deleteExpenseById(id: Long) = dao.deleteById(id)

    override suspend fun getExpenseById(id: Long): ExpenseEntity? = dao.getExpenseById(id)

    override fun getAllExpenses(): Flow<List<ExpenseEntity>> = dao.getAllExpenses()

    override fun getRecentExpenses(limit: Int): Flow<List<ExpenseEntity>> = dao.getRecentExpenses(limit)

    override fun getExpensesBetween(startTime: Instant, endTime: Instant): Flow<List<ExpenseEntity>> =
        dao.getExpensesBetween(startTime, endTime)

    override fun getTotalSpentBetween(startTime: Instant, endTime: Instant): Flow<Double> =
        dao.getTotalSpentBetween(startTime, endTime)

    override fun getCategoryBreakdown(startTime: Instant, endTime: Instant): Flow<List<CategorySum>> =
        dao.getCategoryBreakdownBetween(startTime, endTime)

    override fun searchExpenses(query: String, startTime: Instant, endTime: Instant): Flow<List<ExpenseEntity>> =
        dao.searchExpenses(query, startTime, endTime)

    override fun getExpensesByDate(date: LocalDate): Flow<List<ExpenseEntity>> {
        val start = date.atStartOfDay(zoneId).toInstant()
        val end = date.atTime(LocalTime.MAX).atZone(zoneId).toInstant()
        return dao.getExpensesBetween(start, end)
    }

    override fun getDailySpendingForMonth(yearMonth: LocalDate): Flow<Map<LocalDate, Double>> {
        val startOfMonth = yearMonth.withDayOfMonth(1).atStartOfDay(zoneId).toInstant()
        val endOfMonth = YearMonth.from(yearMonth).atEndOfMonth().atTime(LocalTime.MAX).atZone(zoneId).toInstant()

        return dao.getExpensesBetween(startOfMonth, endOfMonth).map { expenses ->
            val dailyMap = mutableMapOf<LocalDate, Double>()
            for (expense in expenses) {
                val expenseDate = expense.timestamp.atZone(zoneId).toLocalDate()
                dailyMap[expenseDate] = (dailyMap[expenseDate] ?: 0.0) + expense.amount
            }
            dailyMap
        }
    }

    override fun getDashboardSummary(): Flow<DashboardSummary> {
        val today = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1).atStartOfDay(zoneId).toInstant()
        val endOfMonth = YearMonth.from(today).atEndOfMonth().atTime(LocalTime.MAX).atZone(zoneId).toInstant()

        val startOfToday = today.atStartOfDay(zoneId).toInstant()
        val endOfToday = today.atTime(LocalTime.MAX).atZone(zoneId).toInstant()

        val yesterday = today.minusDays(1)
        val startOfYesterday = yesterday.atStartOfDay(zoneId).toInstant()
        val endOfYesterday = yesterday.atTime(LocalTime.MAX).atZone(zoneId).toInstant()

        val sevenDaysAgo = today.minusDays(6)
        val startOfSevenDays = sevenDaysAgo.atStartOfDay(zoneId).toInstant()

        val monthTotalFlow = dao.getTotalSpentBetween(startOfMonth, endOfMonth)
        val todayTotalFlow = dao.getTotalSpentBetween(startOfToday, endOfToday)
        val yesterdayTotalFlow = dao.getTotalSpentBetween(startOfYesterday, endOfYesterday)
        val sevenDaysExpensesFlow = dao.getExpensesBetween(startOfSevenDays, endOfToday)
        val recentFlow = dao.getRecentExpenses(5)

        return combine(
            monthTotalFlow,
            todayTotalFlow,
            yesterdayTotalFlow,
            sevenDaysExpensesFlow,
            recentFlow
        ) { monthTotal, todayTotal, yesterdayTotal, sevenDaysExpenses, recentExpenses ->

            // Aggregate 7-day trend
            val dailySpendingList = (0..6).map { dayOffset ->
                val date = sevenDaysAgo.plusDays(dayOffset.toLong())
                val dayStart = date.atStartOfDay(zoneId).toInstant()
                val dayEnd = date.atTime(LocalTime.MAX).atZone(zoneId).toInstant()

                val dayExpenses = sevenDaysExpenses.filter { it.timestamp in dayStart..dayEnd }
                val dayTotal = dayExpenses.sumOf { it.amount }

                val categoryMap = mutableMapOf<Category, Double>()
                dayExpenses.forEach { exp ->
                    categoryMap[exp.category] = (categoryMap[exp.category] ?: 0.0) + exp.amount
                }

                DailySpending(
                    date = date,
                    dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    totalAmount = dayTotal,
                    isToday = date == today,
                    categoryBreakdown = categoryMap
                )
            }

            val runningTotal7Days = sevenDaysExpenses.sumOf { it.amount }
            val budgetStatus = BudgetStatus(
                monthlyBudget = 35000.0,
                totalSpentThisMonth = monthTotal,
                dailyLimit = 1200.0
            )

            DashboardSummary(
                totalSpentMonth = monthTotal,
                todaySpent = todayTotal,
                yesterdaySpent = yesterdayTotal,
                runningTotal7Days = runningTotal7Days,
                budgetStatus = budgetStatus,
                weeklyTrend = dailySpendingList,
                recentTransactions = recentExpenses
            )
        }
    }
}
