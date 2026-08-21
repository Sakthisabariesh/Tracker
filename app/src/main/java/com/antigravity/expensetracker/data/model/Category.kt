package com.antigravity.expensetracker.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LaptopMac
import androidx.compose.material.icons.rounded.LocalMall
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class Category(
    val displayName: String,
    val hexColor: Long,
    val isIncome: Boolean = false
) {
    // Expense Categories
    FOOD("Dining & Food", 0xFFFF7043),
    TRAVEL("Transport", 0xFF29B6F6),
    BILLS("Bills & Utilities", 0xFF9575CD),
    SHOPPING("Shopping", 0xFFEC407A),
    GROCERIES("Groceries", 0xFF66BB6A),
    ENTERTAINMENT("Entertainment", 0xFFFFCA28),
    HEALTH("Health & Fitness", 0xFF26A69A),
    OTHERS("Miscellaneous", 0xFF78909C),

    // Income Categories
    SALARY("Salary & Wage", 0xFF00E676, isIncome = true),
    FREELANCE("Freelance & Side Gig", 0xFF00B0FF, isIncome = true),
    INVESTMENTS("Investments & Returns", 0xFF7C4DFF, isIncome = true),
    CASHBACK("Cashback & Rewards", 0xFFFFD600, isIncome = true);

    fun getIcon(): ImageVector = when (this) {
        FOOD -> Icons.Rounded.Fastfood
        TRAVEL -> Icons.Rounded.DirectionsBus
        BILLS -> Icons.Rounded.ReceiptLong
        SHOPPING -> Icons.Rounded.LocalMall
        GROCERIES -> Icons.Rounded.ShoppingCart
        ENTERTAINMENT -> Icons.Rounded.Movie
        HEALTH -> Icons.Rounded.FitnessCenter
        OTHERS -> Icons.Rounded.MoreHoriz
        SALARY -> Icons.Rounded.Payments
        FREELANCE -> Icons.Rounded.LaptopMac
        INVESTMENTS -> Icons.Rounded.AccountBalance
        CASHBACK -> Icons.Rounded.Savings
    }

    fun getColor(): Color = Color(hexColor)

    companion object {
        val expenseCategories = entries.filter { !it.isIncome }
        val incomeCategories = entries.filter { it.isIncome }

        fun fromString(value: String): Category {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: OTHERS
        }
    }
}
