package com.antigravity.expensetracker.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("expense_tracker_prefs", Context.MODE_PRIVATE)

    private val _monthlyBudget = MutableStateFlow(prefs.getFloat(KEY_MONTHLY_BUDGET, 35000f).toDouble())
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    private val _dailyLimit = MutableStateFlow(prefs.getFloat(KEY_DAILY_LIMIT, 1200f).toDouble())
    val dailyLimit: StateFlow<Double> = _dailyLimit.asStateFlow()

    fun updateMonthlyBudget(budget: Double) {
        prefs.edit().putFloat(KEY_MONTHLY_BUDGET, budget.toFloat()).apply()
        _monthlyBudget.value = budget
    }

    fun updateDailyLimit(limit: Double) {
        prefs.edit().putFloat(KEY_DAILY_LIMIT, limit.toFloat()).apply()
        _dailyLimit.value = limit
    }

    companion object {
        private const val KEY_MONTHLY_BUDGET = "key_monthly_budget"
        private const val KEY_DAILY_LIMIT = "key_daily_limit"
    }
}
