package com.antigravity.expensetracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.antigravity.expensetracker.data.model.Category
import com.antigravity.expensetracker.data.model.ExpenseEntity
import com.antigravity.expensetracker.data.model.PaymentMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

@Database(
    entities = [ExpenseEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: ExpenseDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope): ExpenseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseDatabase::class.java,
                    "expense_tracker_database"
                )
                    .addCallback(ExpenseDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class ExpenseDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.expenseDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: ExpenseDao) {
                val now = Instant.now()
                val sampleData = listOf(
                    // Today
                    ExpenseEntity(
                        title = "Healthy Brunch & Coffee",
                        amount = 380.0,
                        category = Category.FOOD,
                        paymentMode = PaymentMode.UPI,
                        timestamp = now.minus(2, ChronoUnit.HOURS),
                        notes = "Blue Tokai Coffee"
                    ),
                    ExpenseEntity(
                        title = "Cab to Office",
                        amount = 220.0,
                        category = Category.TRAVEL,
                        paymentMode = PaymentMode.UPI,
                        timestamp = now.minus(5, ChronoUnit.HOURS),
                        notes = "Ola Prime"
                    ),
                    // Yesterday
                    ExpenseEntity(
                        title = "Weekly Grocery Staples",
                        amount = 1450.0,
                        category = Category.GROCERIES,
                        paymentMode = PaymentMode.CREDIT_CARD,
                        timestamp = now.minus(1, ChronoUnit.DAYS).minus(4, ChronoUnit.HOURS),
                        notes = "Nature's Basket"
                    ),
                    ExpenseEntity(
                        title = "Electricity & Wifi Bill",
                        amount = 1890.0,
                        category = Category.BILLS,
                        paymentMode = PaymentMode.DEBIT_CARD,
                        timestamp = now.minus(1, ChronoUnit.DAYS).minus(8, ChronoUnit.HOURS),
                        notes = "Airtel Broadband & Bescom"
                    ),
                    // 2 Days ago
                    ExpenseEntity(
                        title = "Team Dinner",
                        amount = 1200.0,
                        category = Category.FOOD,
                        paymentMode = PaymentMode.CREDIT_CARD,
                        timestamp = now.minus(2, ChronoUnit.DAYS).minus(3, ChronoUnit.HOURS),
                        notes = "Burmese Kitchen"
                    ),
                    // 3 Days ago
                    ExpenseEntity(
                        title = "Running Shoes",
                        amount = 3499.0,
                        category = Category.SHOPPING,
                        paymentMode = PaymentMode.CREDIT_CARD,
                        timestamp = now.minus(3, ChronoUnit.DAYS).minus(6, ChronoUnit.HOURS),
                        notes = "Nike React Pegasus"
                    ),
                    // 4 Days ago
                    ExpenseEntity(
                        title = "Metro Card Recharge",
                        amount = 500.0,
                        category = Category.TRAVEL,
                        paymentMode = PaymentMode.UPI,
                        timestamp = now.minus(4, ChronoUnit.DAYS).minus(5, ChronoUnit.HOURS),
                        notes = "Monthly commuter pass"
                    ),
                    // 5 Days ago
                    ExpenseEntity(
                        title = "Movie IMAX Tickets",
                        amount = 860.0,
                        category = Category.ENTERTAINMENT,
                        paymentMode = PaymentMode.UPI,
                        timestamp = now.minus(5, ChronoUnit.DAYS).minus(7, ChronoUnit.HOURS),
                        notes = "BookMyShow"
                    ),
                    // 6 Days ago
                    ExpenseEntity(
                        title = "Pharmacy & Supplements",
                        amount = 640.0,
                        category = Category.OTHERS,
                        paymentMode = PaymentMode.CASH,
                        timestamp = now.minus(6, ChronoUnit.DAYS).minus(2, ChronoUnit.HOURS),
                        notes = "Apollo Pharmacy"
                    )
                )
                dao.insertAll(sampleData)
            }
        }
    }
}
