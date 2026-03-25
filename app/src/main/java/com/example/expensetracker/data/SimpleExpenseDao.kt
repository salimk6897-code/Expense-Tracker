package com.example.expensetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SimpleExpenseDao {
    @Insert
    suspend fun insertExpense(expense: SimpleExpenseEntity)

    @Query("SELECT * FROM personal_expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<SimpleExpenseEntity>>

    @Query("DELETE FROM personal_expenses WHERE id = :id")
    suspend fun deleteExpense(id: Long)

    @Query("DELETE FROM personal_expenses")
    suspend fun clearAll()
}
