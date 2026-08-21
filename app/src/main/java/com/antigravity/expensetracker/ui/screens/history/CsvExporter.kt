package com.antigravity.expensetracker.ui.screens.history

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.antigravity.expensetracker.data.model.ExpenseEntity
import java.io.File
import java.io.FileWriter
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object CsvExporter {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.systemDefault())

    fun exportToCsv(context: Context, expenses: List<ExpenseEntity>): File {
        val file = File(context.cacheDir, "ExpenseTracker_Export_${System.currentTimeMillis()}.csv")
        FileWriter(file).use { writer ->
            writer.append("ID,Date,Title,Category,PaymentMode,Amount (INR),Notes\n")
            for (expense in expenses) {
                val formattedDate = dateFormatter.format(expense.timestamp)
                val safeTitle = escapeCsv(expense.title)
                val safeNotes = escapeCsv(expense.notes)
                writer.append("${expense.id},\"$formattedDate\",\"$safeTitle\",\"${expense.category.displayName}\",\"${expense.paymentMode.displayName}\",${expense.amount},\"$safeNotes\"\n")
            }
        }
        return file
    }

    fun shareExportFile(context: Context, file: File) {
        val uri = try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            // Fallback for simple share if fileprovider is not in xml
            android.net.Uri.fromFile(file)
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Expense Tracker Backup")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export Expenses"))
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }
}
