package com.antigravity.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.antigravity.expensetracker.ui.navigation.ExpenseAppNav
import com.antigravity.expensetracker.ui.theme.ExpenseTrackerTheme
import com.antigravity.expensetracker.ui.viewmodel.ExpenseViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ExpenseViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = application as ExpenseTrackerApp
                return ExpenseViewModel(app.repository, app.userPreferences) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            ExpenseTrackerTheme {
                val navController = rememberNavController()
                ExpenseAppNav(
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }
}
