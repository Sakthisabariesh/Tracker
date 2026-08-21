package com.antigravity.expensetracker

import android.app.Application
import com.antigravity.expensetracker.data.local.ExpenseDatabase
import com.antigravity.expensetracker.data.local.UserPreferences
import com.antigravity.expensetracker.data.repository.ExpenseRepositoryImpl
import com.antigravity.expensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class ExpenseTrackerApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: ExpenseDatabase by lazy {
        ExpenseDatabase.getInstance(this, applicationScope)
    }

    val userPreferences: UserPreferences by lazy {
        UserPreferences(this)
    }

    val repository: ExpenseRepository by lazy {
        ExpenseRepositoryImpl(database.expenseDao())
    }
}
