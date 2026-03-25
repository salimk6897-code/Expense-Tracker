package com.example.expensetracker.repository

import com.example.expensetracker.data.MemberDao
import com.example.expensetracker.data.MemberEntity
import com.example.expensetracker.data.SplitExpenseDao
import com.example.expensetracker.data.SplitExpenseEntity
import kotlinx.coroutines.flow.Flow

class SplitExpenseRepository(
    private val memberDao: MemberDao,
    private val splitExpenseDao: SplitExpenseDao
) {
    val allMembers: Flow<List<MemberEntity>> = memberDao.getAllMembers()
    val allExpenses: Flow<List<SplitExpenseEntity>> = splitExpenseDao.getAllExpenses()

    suspend fun addMember(member: MemberEntity) {
        memberDao.insertMember(member)
    }

    suspend fun deleteMember(name: String) {
        memberDao.deleteMember(name)
    }

    suspend fun addExpense(expense: SplitExpenseEntity) {
        splitExpenseDao.insertExpense(expense)
    }

    suspend fun deleteExpense(id: Long) {
        splitExpenseDao.deleteExpense(id)
    }

    fun getExpensesByGroup(groupId: Long): Flow<List<SplitExpenseEntity>> {
        // We'll add this query to SplitExpenseDao next
        return splitExpenseDao.getExpensesByGroup(groupId)
    }
}
