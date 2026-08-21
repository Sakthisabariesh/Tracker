package com.antigravity.expensetracker.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.antigravity.expensetracker.data.model.ExpenseEntity
import com.antigravity.expensetracker.ui.components.BudgetProgressCard
import com.antigravity.expensetracker.ui.components.EditExpenseDialog
import com.antigravity.expensetracker.ui.components.HeroMetricsCard
import com.antigravity.expensetracker.ui.components.SpendingBarChart
import com.antigravity.expensetracker.ui.components.TransactionItem
import com.antigravity.expensetracker.ui.theme.SpendingRed
import com.antigravity.expensetracker.ui.viewmodel.DashboardUiState
import com.antigravity.expensetracker.ui.viewmodel.ExpenseEvent
import com.antigravity.expensetracker.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ExpenseViewModel,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dashboardState by viewModel.dashboardUiState.collectAsState()
    val quickDraft by viewModel.quickLogDraft.collectAsState()
    val editingExpense by viewModel.editingExpense.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var isBottomSheetOpen by remember { mutableStateOf(false) }
    var isSettingsDialogOpen by remember { mutableStateOf(false) }
    var isConfirmClearDialogOpen by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Listen for user feedback / undo messages
    LaunchedEffect(key1 = true) {
        viewModel.userMessage.collectLatest { message ->
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = if (message.contains("deleted")) "UNDO" else null,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.onEvent(ExpenseEvent.OnUndoDelete)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isBottomSheetOpen = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.size(62.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Quick Log Expense",
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->

        when (val state = dashboardState) {
            is DashboardUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is DashboardUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            is DashboardUiState.Success -> {
                val summary = state.summary

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header App Brand with Settings button
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ExpensePulse",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Zero-friction spend intelligence",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { isSettingsDialogOpen = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 1. Hero Metrics Card
                    item {
                        HeroMetricsCard(summary = summary)
                    }

                    // 2. Monthly Budget Progress Card (with in-place budget edit)
                    item {
                        BudgetProgressCard(
                            budgetStatus = summary.budgetStatus,
                            onUpdateBudget = { viewModel.onEvent(ExpenseEvent.OnUpdateMonthlyBudget(it)) }
                        )
                    }

                    // 3. 7-Day Spending Bar Chart (Compose Canvas 120Hz)
                    item {
                        SpendingBarChart(
                            weeklyTrend = summary.weeklyTrend,
                            dailyLimit = summary.budgetStatus.dailyLimit
                        )
                    }

                    // 4. Recent Transactions Section Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Transactions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(onClick = onNavigateToHistory) {
                                Text(
                                    text = "View All",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // 5. Recent 5 Transactions Feed (with Tap to Edit & Swipe to delete)
                    if (summary.recentTransactions.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Receipt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No expenses logged yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Tap the + button to log your first spend",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else {
                        items(
                            items = summary.recentTransactions,
                            key = { it.id }
                        ) { expense ->
                            Box(
                                modifier = Modifier.clickable {
                                    viewModel.onEvent(ExpenseEvent.OnSelectExpenseForEdit(expense))
                                }
                            ) {
                                TransactionItem(
                                    expense = expense,
                                    onDelete = { viewModel.onEvent(ExpenseEvent.OnDeleteExpense(it)) }
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            }
        }

        // Quick Log Bottom Sheet
        if (isBottomSheetOpen) {
            AddExpenseBottomSheet(
                sheetState = sheetState,
                draft = quickDraft,
                onEvent = viewModel::onEvent,
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        isBottomSheetOpen = false
                        viewModel.onEvent(ExpenseEvent.OnResetDraft)
                    }
                }
            )
        }

        // Edit Expense Modal
        editingExpense?.let { expense ->
            EditExpenseDialog(
                expense = expense,
                sheetState = editSheetState,
                onSave = { viewModel.onEvent(ExpenseEvent.OnUpdateExpense(it)) },
                onDelete = { viewModel.onEvent(ExpenseEvent.OnDeleteExpense(it)) },
                onDismiss = { viewModel.onEvent(ExpenseEvent.OnSelectExpenseForEdit(null)) }
            )
        }

        // Settings Dialog (Budget & Data Management)
        if (isSettingsDialogOpen && dashboardState is DashboardUiState.Success) {
            val summary = (dashboardState as DashboardUiState.Success).summary
            var budgetInput by remember { mutableStateOf(summary.budgetStatus.monthlyBudget.toInt().toString()) }
            var limitInput by remember { mutableStateOf(summary.budgetStatus.dailyLimit.toInt().toString()) }

            AlertDialog(
                onDismissRequest = { isSettingsDialogOpen = false },
                title = { Text("App Settings & Caps") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        OutlinedTextField(
                            value = budgetInput,
                            onValueChange = { budgetInput = it.filter { c -> c.isDigit() } },
                            label = { Text("Monthly Budget Cap (₹)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = limitInput,
                            onValueChange = { limitInput = it.filter { c -> c.isDigit() } },
                            label = { Text("Daily Target Limit (₹)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Reset / Wipe Data Button
                        OutlinedButton(
                            onClick = {
                                isSettingsDialogOpen = false
                                isConfirmClearDialogOpen = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Rounded.DeleteSweep, contentDescription = null, tint = SpendingRed)
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Clear All Data (Start Fresh)", color = SpendingRed)
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            budgetInput.toDoubleOrNull()?.let {
                                viewModel.onEvent(ExpenseEvent.OnUpdateMonthlyBudget(it))
                            }
                            limitInput.toDoubleOrNull()?.let {
                                viewModel.onEvent(ExpenseEvent.OnUpdateDailyLimit(it))
                            }
                            isSettingsDialogOpen = false
                        }
                    ) {
                        Text("Save Caps")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isSettingsDialogOpen = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Confirmation Dialog to Clear Sample Data
        if (isConfirmClearDialogOpen) {
            AlertDialog(
                onDismissRequest = { isConfirmClearDialogOpen = false },
                title = { Text("Clear All Expenses?") },
                text = {
                    Text("This will remove all sample/logged transactions so you can start completely fresh with zero expenses.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.onEvent(ExpenseEvent.OnClearAllExpenses)
                            isConfirmClearDialogOpen = false
                        }
                    ) {
                        Text("Yes, Clear All", color = SpendingRed, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isConfirmClearDialogOpen = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
