package com.antigravity.expensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.expensetracker.data.local.UserPreferences
import com.antigravity.expensetracker.data.model.Category
import com.antigravity.expensetracker.data.model.ExpenseEntity
import com.antigravity.expensetracker.data.model.PaymentMode
import com.antigravity.expensetracker.domain.model.BudgetStatus
import com.antigravity.expensetracker.domain.model.DashboardSummary
import com.antigravity.expensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModel(
    private val repository: ExpenseRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val zoneId = ZoneId.systemDefault()

    // Preferences Flows
    val monthlyBudget = userPreferences.monthlyBudget
    val dailyLimit = userPreferences.dailyLimit

    // Dashboard State
    val dashboardUiState: StateFlow<DashboardUiState> = combine(
        repository.getDashboardSummary(),
        monthlyBudget,
        dailyLimit
    ) { summary, budget, limit ->
        summary.copy(
            budgetStatus = BudgetStatus(
                monthlyBudget = budget,
                totalSpentThisMonth = summary.totalSpentMonth,
                dailyLimit = limit
            )
        )
    }.map<DashboardSummary, DashboardUiState> { DashboardUiState.Success(it) }
    .catch { emit(DashboardUiState.Error(it.message ?: "Failed to load dashboard")) }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState.Loading
    )

    // Quick Log Draft
    private val _quickLogDraft = MutableStateFlow(QuickLogDraft())
    val quickLogDraft: StateFlow<QuickLogDraft> = _quickLogDraft.asStateFlow()

    // Edit Item Modal State
    private val _editingExpense = MutableStateFlow<ExpenseEntity?>(null)
    val editingExpense: StateFlow<ExpenseEntity?> = _editingExpense.asStateFlow()

    // Deleted item for Undo
    private var recentlyDeletedExpense: ExpenseEntity? = null
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // History Filter Parameters
    private val _historySearchQuery = MutableStateFlow("")
    private val _historyDateFilter = MutableStateFlow(DateFilterRange.THIS_MONTH)
    private val _historyCategoryFilter = MutableStateFlow<Category?>(null)
    private val _historyPaymentFilter = MutableStateFlow<PaymentMode?>(null)

    // Combined History State
    val historyUiState: StateFlow<HistoryUiState> = combine(
        _historySearchQuery,
        _historyDateFilter,
        _historyCategoryFilter,
        _historyPaymentFilter
    ) { query, dateFilter, category, paymentMode ->
        HistoryFilterParams(query, dateFilter, category, paymentMode)
    }.flatMapLatest { params ->
        val now = LocalDate.now()
        val (startTime, endTime) = when (params.dateFilter) {
            DateFilterRange.THIS_WEEK -> {
                val startOfWeek = now.minusDays(now.dayOfWeek.value.toLong() - 1)
                Pair(
                    startOfWeek.atStartOfDay(zoneId).toInstant(),
                    now.atTime(LocalTime.MAX).atZone(zoneId).toInstant()
                )
            }
            DateFilterRange.THIS_MONTH -> {
                val startOfMonth = now.withDayOfMonth(1)
                val endOfMonth = YearMonth.from(now).atEndOfMonth()
                Pair(
                    startOfMonth.atStartOfDay(zoneId).toInstant(),
                    endOfMonth.atTime(LocalTime.MAX).atZone(zoneId).toInstant()
                )
            }
            DateFilterRange.ALL_TIME -> {
                Pair(
                    Instant.EPOCH,
                    Instant.now().plusSeconds(86400 * 365)
                )
            }
        }

        if (params.query.isBlank()) {
            repository.getExpensesBetween(startTime, endTime)
        } else {
            repository.searchExpenses(params.query, startTime, endTime)
        }.map { list ->
            val filtered = list.filter { item ->
                (params.category == null || item.category == params.category) &&
                (params.paymentMode == null || item.paymentMode == params.paymentMode)
            }
            HistoryUiState(
                searchQuery = params.query,
                selectedFilterRange = params.dateFilter,
                selectedCategory = params.category,
                selectedPaymentMode = params.paymentMode,
                expenses = filtered,
                totalFilteredSpend = filtered.sumOf { it.amount },
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState(isLoading = true)
    )

    // Calendar Screen State
    private val _selectedCalendarMonth = MutableStateFlow(LocalDate.now())
    private val _selectedCalendarDate = MutableStateFlow(LocalDate.now())

    val calendarUiState: StateFlow<CalendarUiState> = combine(
        _selectedCalendarMonth,
        _selectedCalendarDate
    ) { month, date ->
        Pair(month, date)
    }.flatMapLatest { (month, selectedDate) ->
        combine(
            repository.getDailySpendingForMonth(month),
            repository.getExpensesByDate(selectedDate)
        ) { dailyMap, dayExpenses ->
            CalendarUiState(
                selectedMonth = month,
                selectedDate = selectedDate,
                dailySpendMap = dailyMap,
                selectedDayExpenses = dayExpenses,
                selectedDayTotal = dayExpenses.sumOf { it.amount },
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarUiState(isLoading = true)
    )

    fun onEvent(event: ExpenseEvent) {
        when (event) {
            is ExpenseEvent.OnKeypadPress -> handleKeypadInput(event.char)
            ExpenseEvent.OnKeypadBackspace -> handleKeypadBackspace()
            ExpenseEvent.OnKeypadClear -> _quickLogDraft.update { it.copy(amountString = "") }
            is ExpenseEvent.OnTitleChange -> _quickLogDraft.update { it.copy(title = event.title) }
            is ExpenseEvent.OnCategorySelect -> _quickLogDraft.update { it.copy(category = event.category) }
            is ExpenseEvent.OnPaymentModeSelect -> _quickLogDraft.update { it.copy(paymentMode = event.paymentMode) }
            is ExpenseEvent.OnDateSelect -> _quickLogDraft.update { it.copy(date = event.date) }
            is ExpenseEvent.OnNotesChange -> _quickLogDraft.update { it.copy(notes = event.notes) }
            ExpenseEvent.OnResetDraft -> _quickLogDraft.value = QuickLogDraft()
            ExpenseEvent.OnSaveExpense -> saveQuickLogExpense()

            is ExpenseEvent.OnSelectExpenseForEdit -> _editingExpense.value = event.expense
            is ExpenseEvent.OnUpdateExpense -> {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.updateExpense(event.expense)
                    _editingExpense.value = null
                    _userMessage.emit("Updated ${event.expense.title}")
                }
            }

            is ExpenseEvent.OnUpdateMonthlyBudget -> userPreferences.updateMonthlyBudget(event.budget)
            is ExpenseEvent.OnUpdateDailyLimit -> userPreferences.updateDailyLimit(event.limit)

            ExpenseEvent.OnClearAllExpenses -> {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.deleteAllExpenses()
                    _userMessage.emit("All expenses cleared. Fresh start ready!")
                }
            }

            is ExpenseEvent.OnDeleteExpense -> {
                recentlyDeletedExpense = event.expense
                viewModelScope.launch(Dispatchers.IO) {
                    repository.deleteExpense(event.expense)
                    _userMessage.emit("Expense deleted")
                }
            }
            ExpenseEvent.OnUndoDelete -> {
                recentlyDeletedExpense?.let { expense ->
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.insertExpense(expense.copy(id = 0L))
                        recentlyDeletedExpense = null
                        _userMessage.emit("Expense restored")
                    }
                }
            }

            is ExpenseEvent.OnSearchQueryChange -> _historySearchQuery.value = event.query
            is ExpenseEvent.OnDateFilterChange -> _historyDateFilter.value = event.filter
            is ExpenseEvent.OnCategoryFilterToggle -> {
                _historyCategoryFilter.update { current ->
                    if (current == event.category) null else event.category
                }
            }
            is ExpenseEvent.OnPaymentModeFilterToggle -> {
                _historyPaymentFilter.update { current ->
                    if (current == event.paymentMode) null else event.paymentMode
                }
            }

            is ExpenseEvent.OnCalendarMonthChange -> _selectedCalendarMonth.value = event.yearMonth
            is ExpenseEvent.OnCalendarDateSelected -> _selectedCalendarDate.value = event.date
        }
    }

    private fun handleKeypadInput(char: String) {
        _quickLogDraft.update { current ->
            val curr = current.amountString
            if (char == ".") {
                if (curr.isEmpty()) {
                    current.copy(amountString = "0.")
                } else if (!curr.contains(".")) {
                    current.copy(amountString = "$curr.")
                } else {
                    current
                }
            } else {
                if (curr.replace(".", "").length >= 7) {
                    return@update current
                }
                if (curr.contains(".") && curr.substringAfter(".").length >= 2) {
                    return@update current
                }
                if (curr == "0") {
                    current.copy(amountString = char)
                } else {
                    current.copy(amountString = curr + char)
                }
            }
        }
    }

    private fun handleKeypadBackspace() {
        _quickLogDraft.update { current ->
            val curr = current.amountString
            if (curr.isNotEmpty()) {
                current.copy(amountString = curr.dropLast(1))
            } else {
                current
            }
        }
    }

    private fun saveQuickLogExpense() {
        val draft = _quickLogDraft.value
        if (!draft.isValid) return

        val title = if (draft.title.isNotBlank()) draft.title else "${draft.category.displayName} Spend"
        val instant = draft.date.atTime(LocalTime.now()).atZone(zoneId).toInstant()

        val entity = ExpenseEntity(
            title = title,
            amount = draft.amount,
            category = draft.category,
            paymentMode = draft.paymentMode,
            timestamp = instant,
            notes = draft.notes
        )

        viewModelScope.launch(Dispatchers.IO) {
            repository.insertExpense(entity)
            _quickLogDraft.value = QuickLogDraft()
            _userMessage.emit("Logged ₹${entity.amount.toInt()} for ${entity.title}")
        }
    }

    private data class HistoryFilterParams(
        val query: String,
        val dateFilter: DateFilterRange,
        val category: Category?,
        val paymentMode: PaymentMode?
    )
}
