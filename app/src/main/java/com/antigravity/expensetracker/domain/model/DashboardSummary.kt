package com.antigravity.expensetracker.domain.model

import com.antigravity.expensetracker.data.model.Category
import com.antigravity.expensetracker.data.model.ExpenseEntity
import java.time.LocalDate

data class DailySpending(
    val date: LocalDate,
    val dayLabel: String,
    val totalAmount: Double,
    val isToday: Boolean = false,
    val categoryBreakdown: Map<Category, Double> = emptyMap()
)

data class DayExpenseSummary(
    val date: LocalDate,
    val totalAmount: Double,
    val expenses: List<ExpenseEntity>
)

data class BudgetStatus(
    val monthlyBudget: Double = 35000.0,
    val totalSpentThisMonth: Double = 0.0,
    val dailyLimit: Double = 1200.0
) {
    val percentageUsed: Float
        get() = if (monthlyBudget > 0) ((totalSpentThisMonth / monthlyBudget).toFloat()).coerceIn(0f, 1.5f) else 0f

    val isWarning: Boolean
        get() = percentageUsed >= 0.80f && percentageUsed < 1.0f

    val isExceeded: Boolean
        get() = percentageUsed >= 1.0f

    val remainingAmount: Double
        get() = (monthlyBudget - totalSpentThisMonth).coerceAtLeast(0.0)
}

data class DashboardSummary(
    val totalSpentMonth: Double = 0.0,
    val todaySpent: Double = 0.0,
    val yesterdaySpent: Double = 0.0,
    val runningTotal7Days: Double = 0.0,
    val budgetStatus: BudgetStatus = BudgetStatus(),
    val weeklyTrend: List<DailySpending> = emptyList(),
    val recentTransactions: List<ExpenseEntity> = emptyList()
) {
    val todayDeltaPercentage: Double
        get() = if (yesterdaySpent > 0) {
            ((todaySpent - yesterdaySpent) / yesterdaySpent) * 100.0
        } else if (todaySpent > 0) {
            100.0
        } else {
            0.0
        }
}
