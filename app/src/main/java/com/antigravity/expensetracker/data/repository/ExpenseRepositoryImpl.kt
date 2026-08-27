package com.antigravity.expensetracker.data.repository

import com.antigravity.expensetracker.data.local.CategorySum
import com.antigravity.expensetracker.data.local.ExpenseDao
import com.antigravity.expensetracker.data.model.Category
import com.antigravity.expensetracker.data.model.ExpenseEntity
import com.antigravity.expensetracker.data.model.TransactionType
import com.antigravity.expensetracker.domain.model.BudgetStatus
import com.antigravity.expensetracker.domain.model.DailySpending
import com.antigravity.expensetracker.domain.model.DashboardSummary
import com.antigravity.expensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
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

    private data class DashboardTotals(
        val monthExpense: Double,
        val monthIncome: Double,
        val todayTotal: Double,
        val yesterdayTotal: Double,
        val sevenDaysExpenses: List<ExpenseEntity>
    )

    private val zoneId = ZoneId.systemDefault()

    override suspend fun insertExpense(expense: ExpenseEntity): Long = dao.insertExpense(expense)

    override suspend fun updateExpense(expense: ExpenseEntity) = dao.updateExpense(expense)

    override suspend fun deleteExpense(expense: ExpenseEntity) = dao.deleteExpense(expense)

    override suspend fun deleteExpenseById(id: Long) = dao.deleteById(id)

    override suspend fun deleteAllExpenses() = dao.deleteAllExpenses()

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
                if (expense.type == TransactionType.EXPENSE) {
                    val expenseDate = expense.timestamp.atZone(zoneId).toLocalDate()
                    dailyMap[expenseDate] = (dailyMap[expenseDate] ?: 0.0) + expense.amount
                }
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

        val monthExpenseFlow = dao.getTotalSpentBetween(startOfMonth, endOfMonth)
        val monthIncomeFlow = dao.getTotalIncomeBetween(startOfMonth, endOfMonth)
        val todayTotalFlow = dao.getTotalSpentBetween(startOfToday, endOfToday)
        val yesterdayTotalFlow = dao.getTotalSpentBetween(startOfYesterday, endOfYesterday)
        val sevenDaysExpensesFlow = dao.getExpensesBetween(startOfSevenDays, endOfToday)
        val recentFlow = dao.getRecentExpenses(6)

        val dashboardTotalsFlow = combine(
            monthExpenseFlow,
            monthIncomeFlow,
            todayTotalFlow,
            yesterdayTotalFlow,
            sevenDaysExpensesFlow,
        ) { monthExpense, monthIncome, todayTotal, yesterdayTotal, sevenDaysExpenses ->
            DashboardTotals(
                monthExpense = monthExpense,
                monthIncome = monthIncome,
                todayTotal = todayTotal,
                yesterdayTotal = yesterdayTotal,
                sevenDaysExpenses = sevenDaysExpenses
            )
        }

        return combine(dashboardTotalsFlow, recentFlow) { dashboardTotals, recentExpenses ->
            val monthExpense = dashboardTotals.monthExpense
            val monthIncome = dashboardTotals.monthIncome
            val todayTotal = dashboardTotals.todayTotal
            val yesterdayTotal = dashboardTotals.yesterdayTotal
            val sevenDaysExpenses = dashboardTotals.sevenDaysExpenses

            val onlyExpenses7Days = sevenDaysExpenses.filter { it.type == TransactionType.EXPENSE }

            val dailySpendingList = (0..6).map { dayOffset ->
                val date = sevenDaysAgo.plusDays(dayOffset.toLong())
                val dayStart = date.atStartOfDay(zoneId).toInstant()
                val dayEnd = date.atTime(LocalTime.MAX).atZone(zoneId).toInstant()

                val dayExpenses = onlyExpenses7Days.filter { it.timestamp in dayStart..dayEnd }
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
                    isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY,
                    categoryBreakdown = categoryMap
                )
            }

            val runningTotal7Days = onlyExpenses7Days.sumOf { it.amount }
            val budgetStatus = BudgetStatus(
                monthlyBudget = 35000.0,
                totalSpentThisMonth = monthExpense,
                dailyLimit = 1200.0
            )

            DashboardSummary(
                totalSpentMonth = monthExpense,
                totalIncomeMonth = monthIncome,
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
