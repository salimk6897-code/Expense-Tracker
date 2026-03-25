package com.example.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.Currency
import com.example.expensetracker.data.GroupEntity
import com.example.expensetracker.data.MemberEntity
import com.example.expensetracker.data.SplitExpenseEntity
import com.example.expensetracker.repository.GroupRepository
import com.example.expensetracker.repository.SplitExpenseRepository
import com.example.expensetracker.util.SettlementEngine
import com.example.expensetracker.util.Transaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SplitExpenseViewModel(
    private val splitRepo: SplitExpenseRepository,
    private val groupRepo: GroupRepository
) : ViewModel() {

    val allMembers = splitRepo.allMembers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allGroups = groupRepo.allGroups.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedGroupId = MutableStateFlow<Long?>(null)
    val selectedGroupId = _selectedGroupId.asStateFlow()

    private val _selectedCurrency = MutableStateFlow(Currency.INR)
    val selectedCurrency = _selectedCurrency.asStateFlow()

    val currentGroupExpenses: StateFlow<List<SplitExpenseEntity>> = _selectedGroupId.flatMapLatest { groupId ->
        if (groupId == null) splitRepo.allExpenses else splitRepo.getExpensesByGroup(groupId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settlements: StateFlow<List<Transaction>> = currentGroupExpenses.combine(_selectedGroupId) { expenses, _ ->
        SettlementEngine.calculateSettlements(expenses)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectGroup(groupId: Long?) {
        _selectedGroupId.value = groupId
    }

    fun setCurrency(currency: Currency) {
        _selectedCurrency.value = currency
    }

    fun addMember(name: String) {
        viewModelScope.launch {
            splitRepo.addMember(MemberEntity(name))
        }
    }

    fun deleteMember(name: String) {
        viewModelScope.launch {
            splitRepo.deleteMember(name)
        }
    }

    fun createGroup(name: String, description: String, members: List<String>) {
        viewModelScope.launch {
            val groupId = groupRepo.createGroup(name, description, members)
            _selectedGroupId.value = groupId
        }
    }

    fun deleteGroup(groupId: Long) {
        viewModelScope.launch {
            groupRepo.deleteGroup(groupId)
            if (_selectedGroupId.value == groupId) {
                _selectedGroupId.value = null
            }
        }
    }

    fun addExpenseEntity(expense: SplitExpenseEntity) {
        viewModelScope.launch {
            splitRepo.addExpense(expense.copy(groupId = _selectedGroupId.value))
        }
    }

    fun deleteExpense(expenseId: Long) {
        viewModelScope.launch {
            splitRepo.deleteExpense(expenseId)
        }
    }

    companion object {
        fun provideFactory(splitRepo: SplitExpenseRepository, groupRepo: GroupRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SplitExpenseViewModel(splitRepo, groupRepo) as T
                }
            }
    }
}
