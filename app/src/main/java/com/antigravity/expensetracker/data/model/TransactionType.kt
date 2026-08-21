package com.antigravity.expensetracker.data.model

enum class TransactionType(val displayName: String) {
    EXPENSE("Expense"),
    INCOME("Income");

    companion object {
        fun fromString(value: String): TransactionType {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: EXPENSE
        }
    }
}
