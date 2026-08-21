package com.antigravity.expensetracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.History
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : Screen("home", "Dashboard", Icons.Rounded.Dashboard)
    data object Calendar : Screen("calendar", "Calendar", Icons.Rounded.CalendarMonth)
    data object History : Screen("history", "History", Icons.Rounded.History)
}
