package com.antigravity.expensetracker.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.LocalMall
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class Category(
    val displayName: String,
    val hexColor: Long,
    val iconName: String
) {
    FOOD("Food & Dining", 0xFFFF6D00, "Fastfood"),
    TRAVEL("Travel & Commute", 0xFF0091EA, "DirectionsBus"),
    BILLS("Bills & Utilities", 0xFF7C4DFF, "ReceiptLong"),
    SHOPPING("Shopping", 0xFFFF4081, "LocalMall"),
    GROCERIES("Groceries", 0xFF00C853, "ShoppingCart"),
    ENTERTAINMENT("Entertainment", 0xFFAEEA00, "Movie"),
    INVESTMENTS("Investments", 0xFF00B0FF, "AccountBalance"),
    OTHERS("Others", 0xFF78909C, "MoreHoriz");

    fun getIcon(): ImageVector = when (this) {
        FOOD -> Icons.Rounded.Fastfood
        TRAVEL -> Icons.Rounded.DirectionsBus
        BILLS -> Icons.Rounded.ReceiptLong
        SHOPPING -> Icons.Rounded.LocalMall
        GROCERIES -> Icons.Rounded.ShoppingCart
        ENTERTAINMENT -> Icons.Rounded.Movie
        INVESTMENTS -> Icons.Rounded.AccountBalance
        OTHERS -> Icons.Rounded.MoreHoriz
    }

    fun getColor(): Color = Color(hexColor)

    companion object {
        val quickLogCategories = listOf(FOOD, TRAVEL, BILLS, SHOPPING, GROCERIES, OTHERS)

        fun fromString(value: String): Category {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: OTHERS
        }
    }
}
