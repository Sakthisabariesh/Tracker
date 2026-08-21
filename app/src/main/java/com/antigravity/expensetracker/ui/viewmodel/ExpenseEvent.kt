package com.antigravity.expensetracker.ui.viewmodel

import com.antigravity.expensetracker.data.model.Category
import com.antigravity.expensetracker.data.model.ExpenseEntity
import com.antigravity.expensetracker.data.model.PaymentMode
import java.time.LocalDate

sealed interface ExpenseEvent {
    // Quick Log Input
    data class OnKeypadPress(val char: String) : ExpenseEvent
    data object OnKeypadBackspace : ExpenseEvent
    data object OnKeypadClear : ExpenseEvent
    data class OnTitleChange(val title: String) : ExpenseEvent
    data class OnCategorySelect(val category: Category) : ExpenseEvent
    data class OnPaymentModeSelect(val paymentMode: PaymentMode) : ExpenseEvent
    data class OnDateSelect(val date: LocalDate) : ExpenseEvent
    data class OnNotesChange(val notes: String) : ExpenseEvent
    data object OnSaveExpense : ExpenseEvent
    data object OnResetDraft : ExpenseEvent

    // Deletion & Undo
    data class OnDeleteExpense(val expense: ExpenseEntity) : ExpenseEvent
    data object OnUndoDelete : ExpenseEvent

    // History Filtering
    data class OnSearchQueryChange(val query: String) : ExpenseEvent
    data class OnDateFilterChange(val filter: DateFilterRange) : ExpenseEvent
    data class OnCategoryFilterToggle(val category: Category?) : ExpenseEvent
    data class OnPaymentModeFilterToggle(val paymentMode: PaymentMode?) : ExpenseEvent

    // Calendar Inspection
    data class OnCalendarMonthChange(val yearMonth: LocalDate) : ExpenseEvent
    data class OnCalendarDateSelected(val date: LocalDate) : ExpenseEvent
}
