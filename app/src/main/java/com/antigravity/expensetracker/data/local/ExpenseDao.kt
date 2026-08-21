package com.antigravity.expensetracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.antigravity.expensetracker.data.model.Category
import com.antigravity.expensetracker.data.model.ExpenseEntity
import com.antigravity.expensetracker.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.Instant

data class CategorySum(
    val category: Category,
    val total: Double,
    val count: Int
)

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<ExpenseEntity>)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentExpenses(limit: Int = 5): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getExpensesBetween(startTime: Instant, endTime: Instant): Flow<List<ExpenseEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE type = 'EXPENSE' AND timestamp >= :startTime AND timestamp <= :endTime")
    fun getTotalSpentBetween(startTime: Instant, endTime: Instant): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE type = 'INCOME' AND timestamp >= :startTime AND timestamp <= :endTime")
    fun getTotalIncomeBetween(startTime: Instant, endTime: Instant): Flow<Double>

    @Query("""
        SELECT category, SUM(amount) as total, COUNT(id) as count 
        FROM expenses 
        WHERE type = 'EXPENSE' AND timestamp >= :startTime AND timestamp <= :endTime 
        GROUP BY category 
        ORDER BY total DESC
    """)
    fun getCategoryBreakdownBetween(startTime: Instant, endTime: Instant): Flow<List<CategorySum>>

    @Query("""
        SELECT * FROM expenses 
        WHERE (title LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%')
          AND timestamp >= :startTime AND timestamp <= :endTime
        ORDER BY timestamp DESC
    """)
    fun searchExpenses(query: String, startTime: Instant, endTime: Instant): Flow<List<ExpenseEntity>>
}
