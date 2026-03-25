package com.example.expensetracker.data

enum class Screen {
    EXPENSE_TRACKER,
    SPLIT_EXPENSE,
    GROUPS,
    GROUP_DETAILS
}

enum class PayerSelection {
    SINGLE,
    MULTIPLE
}

enum class SplitMethod {
    EQUALLY,
    UNEQUALLY,
    PERCENTAGE,
    SHARES
}

enum class Currency(val symbol: String, val code: String) {
    INR("₹", "INR"),
    USD("$", "USD"),
    EUR("€", "EUR"),
    GBP("£", "GBP")
}

data class Expense(
    val description: String,
    val totalAmount: Double,
    val payers: Map<String, Double>,
    val shares: Map<String, Double>,
    val groupId: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class Group(
    val id: Long = 0,
    val name: String,
    val description: String = ""
)

data class SimpleExpense(
    val name: String,
    val amount: String
)
