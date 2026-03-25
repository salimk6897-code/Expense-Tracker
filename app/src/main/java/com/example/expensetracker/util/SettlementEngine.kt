package com.example.expensetracker.util

import com.example.expensetracker.data.SplitExpenseEntity
import kotlin.math.abs
import kotlin.math.min

data class Transaction(
    val from: String,
    val to: String,
    val amount: Double
)

object SettlementEngine {

    /**
     * Calculates the minimum number of transactions to settle all debts in a group.
     */
    fun calculateSettlements(expenses: List<SplitExpenseEntity>): List<Transaction> {
        val netBalances = mutableMapOf<String, Double>()

        // 1. Calculate net balance for each person
        // Net Balance = (Sum of what they PAID) - (Sum of what they OWE)
        expenses.forEach { expense ->
            expense.payers.forEach { (payer, amount) ->
                netBalances[payer] = (netBalances[payer] ?: 0.0) + amount
            }
            expense.shares.forEach { (borrower, amount) ->
                netBalances[borrower] = (netBalances[borrower] ?: 0.0) - amount
            }
        }

        // 2. Separate people into debtors (negative balance) and creditors (positive balance)
        val debtors = netBalances.filter { it.value < -0.01 }
            .map { it.key to abs(it.value) }
            .sortedByDescending { it.second }
            .toMutableList()

        val creditors = netBalances.filter { it.value > 0.01 }
            .map { it.key to it.value }
            .sortedByDescending { it.second }
            .toMutableList()

        val transactions = mutableListOf<Transaction>()

        // 3. Greedy algorithm to settle debts
        var dIdx = 0
        var cIdx = 0

        while (dIdx < debtors.size && cIdx < creditors.size) {
            val debtor = debtors[dIdx]
            val creditor = creditors[cIdx]

            val settlementAmount = min(debtor.second, creditor.second)
            
            if (settlementAmount > 0.01) {
                transactions.add(Transaction(debtor.first, creditor.first, settlementAmount))
            }

            // Update remaining balances
            debtors[dIdx] = debtor.first to (debtor.second - settlementAmount)
            creditors[cIdx] = creditor.first to (creditor.second - settlementAmount)

            if (debtors[dIdx].second < 0.01) dIdx++
            if (creditors[cIdx].second < 0.01) cIdx++
        }

        return transactions
    }
}
