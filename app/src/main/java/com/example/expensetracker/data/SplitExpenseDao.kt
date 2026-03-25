package com.example.expensetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SplitExpenseDao {
    @Insert
    suspend fun insertExpense(expense: SplitExpenseEntity)

    @Query("SELECT * FROM split_expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<SplitExpenseEntity>>

    @Query("SELECT * FROM split_expenses WHERE groupId = :groupId ORDER BY timestamp DESC")
    fun getExpensesByGroup(groupId: Long): Flow<List<SplitExpenseEntity>>

    @Query("DELETE FROM split_expenses WHERE id = :id")
    suspend fun deleteExpense(id: Long)
}
