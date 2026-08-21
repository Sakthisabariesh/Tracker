package com.antigravity.expensetracker.data.local

import androidx.room.TypeConverter
import com.antigravity.expensetracker.data.model.Category
import com.antigravity.expensetracker.data.model.PaymentMode
import com.antigravity.expensetracker.data.model.TransactionType
import java.time.Instant
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun dateToTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }

    @TypeConverter
    fun fromLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun localDateToString(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun fromCategory(category: Category?): String? {
        return category?.name
    }

    @TypeConverter
    fun toCategory(value: String?): Category? {
        return value?.let { Category.fromString(it) }
    }

    @TypeConverter
    fun fromPaymentMode(paymentMode: PaymentMode?): String? {
        return paymentMode?.name
    }

    @TypeConverter
    fun toPaymentMode(value: String?): PaymentMode? {
        return value?.let { PaymentMode.fromString(it) }
    }

    @TypeConverter
    fun fromTransactionType(type: TransactionType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? {
        return value?.let { TransactionType.fromString(it) }
    }
}
