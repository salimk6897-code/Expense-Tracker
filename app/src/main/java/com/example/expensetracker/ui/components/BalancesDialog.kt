package com.example.expensetracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.expensetracker.data.SplitExpenseEntity
import java.text.DecimalFormat
import kotlin.math.abs

@Composable
fun BalancesDialog(
    expenses: List<SplitExpenseEntity>,
    members: List<String>,
    onDismiss: () -> Unit
) {
    val df = DecimalFormat("#.##")
    val balances = members.associateWith { 0.0 }.toMutableMap()

    expenses.forEach { e ->
        members.forEach { m ->
            val paid = e.payers[m] ?: 0.0
            val share = e.shares[m] ?: 0.0
            balances[m] = (balances[m] ?: 0.0) + (paid - share)
        }
    }

    val debtors = balances.filterValues { it < -0.01 }.toMutableMap()
    val creditors = balances.filterValues { it > 0.01 }.toMutableMap()
    val settlements = mutableListOf<String>()

    while (debtors.isNotEmpty() && creditors.isNotEmpty()) {
        val debtor = debtors.entries.first()
        val creditor = creditors.entries.first()

        val amount = minOf(abs(debtor.value), creditor.value)
        settlements.add("${debtor.key} pays ${creditor.key} ₹${df.format(amount)}")

        val newDebtor = debtor.value + amount
        val newCreditor = creditor.value - amount

        if (abs(newDebtor) < 0.01) debtors.remove(debtor.key) else debtors[debtor.key] = newDebtor
        if (abs(newCreditor) < 0.01) creditors.remove(creditor.key) else creditors[creditor.key] = newCreditor
    }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Group Balances", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(12.dp))

                balances.forEach { (name, bal) ->
                    val text = when {
                        bal > 0.01 -> "Gets back ₹${df.format(bal)}"
                        bal < -0.01 -> "Owes ₹${df.format(abs(bal))}"
                        else -> "Settled"
                    }
                    Text("$name: $text")
                }

                Spacer(Modifier.height(12.dp))
                Divider()
                Spacer(Modifier.height(12.dp))
                Text("Settlement Plan", style = MaterialTheme.typography.titleMedium)

                if (settlements.isEmpty()) Text("Everyone is settled up!")
                else settlements.forEach { Text(it) }

                Spacer(Modifier.height(12.dp))
                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Close")
                }
            }
        }
    }
}
