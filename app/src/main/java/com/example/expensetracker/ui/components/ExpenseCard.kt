package com.example.expensetracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.SplitExpenseEntity
import java.text.DecimalFormat
import kotlin.math.abs

@Composable
fun ExpenseCard(expense: SplitExpenseEntity, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val df = DecimalFormat("#.##")

    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(expense.description, style = MaterialTheme.typography.titleMedium)
                    Text("Total: ₹${df.format(expense.totalAmount)}")
                    val payers = expense.payers.filterValues { it > 0.0 }.keys
                    Text("Paid by: " + (if (payers.size <= 1) payers.firstOrNull() ?: "N/A" else "Multiple"))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }

            if (expanded) {
                Spacer(Modifier.height(12.dp))
                Divider()
                Spacer(Modifier.height(12.dp))

                val members = expense.shares.keys.toList().sorted()
                members.forEach { m ->
                    val paid = expense.payers[m] ?: 0.0
                    val share = expense.shares[m] ?: 0.0
                    val balance = paid - share

                    val text = when {
                        balance > 0.01 -> "Gets back ₹${df.format(balance)}"
                        balance < -0.01 -> "Owes ₹${df.format(abs(balance))}"
                        else -> "Settled"
                    }
                    Text("$m: $text")
                }
            }
        }
    }
}
