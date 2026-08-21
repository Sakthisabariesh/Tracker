package com.antigravity.expensetracker.ui.viewmodel

import com.antigravity.expensetracker.data.model.Category
import com.antigravity.expensetracker.data.model.ExpenseEntity
import com.antigravity.expensetracker.data.model.PaymentMode
import com.antigravity.expensetracker.domain.model.DailySpending
import com.antigravity.expensetracker.domain.model.DashboardSummary
import java.time.LocalDate

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(val summary: DashboardSummary) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

enum class DateFilterRange(val label: String) {
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    ALL_TIME("All Time")
}

data class HistoryUiState(
    val searchQuery: String = "",
    val selectedFilterRange: DateFilterRange = DateFilterRange.THIS_MONTH,
    val selectedCategory: Category? = null,
    val selectedPaymentMode: PaymentMode? = null,
    val expenses: List<ExpenseEntity> = emptyList(),
    val totalFilteredSpend: Double = 0.0,
    val isLoading: Boolean = false
)

data class CalendarUiState(
    val selectedMonth: LocalDate = LocalDate.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val dailySpendMap: Map<LocalDate, Double> = emptyMap(),
    val selectedDayExpenses: List<ExpenseEntity> = emptyList(),
    val selectedDayTotal: Double = 0.0,
    val isLoading: Boolean = false
)

data class QuickLogDraft(
    val amountString: String = "",
    val title: String = "",
    val category: Category = Category.FOOD,
    val paymentMode: PaymentMode = PaymentMode.UPI,
    val date: LocalDate = LocalDate.now(),
    val notes: String = ""
) {
    val amount: Double
        get() = amountString.toDoubleOrNull() ?: 0.0

    val isValid: Boolean
        get() = amount > 0
}
