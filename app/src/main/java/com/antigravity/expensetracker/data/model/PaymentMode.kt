package com.antigravity.expensetracker.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.ui.graphics.vector.ImageVector

enum class PaymentMode(
    val displayName: String,
    val shortTag: String
) {
    UPI("UPI / GPay / PhonePe", "UPI"),
    CASH("Cash in Hand", "CASH"),
    CREDIT_CARD("Credit Card", "CC"),
    DEBIT_CARD("Debit / NetBanking", "BANK");

    fun getIcon(): ImageVector = when (this) {
        UPI -> Icons.Rounded.QrCodeScanner
        CASH -> Icons.Rounded.Payments
        CREDIT_CARD -> Icons.Rounded.CreditCard
        DEBIT_CARD -> Icons.Rounded.AccountBalanceWallet
    }

    companion object {
        fun fromString(value: String): PaymentMode {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: UPI
        }
    }
}
