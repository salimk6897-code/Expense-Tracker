package com.example.expensetracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.Expense
import com.example.expensetracker.data.PayerSelection
import com.example.expensetracker.data.SplitMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    groupMembers: List<String>,
    onDismiss: () -> Unit,
    onAddExpense: (Expense) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var totalAmount by remember { mutableStateOf("") }
    var payerSelection by remember { mutableStateOf(PayerSelection.SINGLE) }
    var singlePayer by remember { mutableStateOf(groupMembers.first()) }
    var multiplePayers by remember { mutableStateOf(groupMembers.associateWith { "" }) }
    var splitMethod by remember { mutableStateOf(SplitMethod.EQUALLY) }
    var unequalShares by remember { mutableStateOf(groupMembers.associateWith { "" }) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense") },
        text = {
            Column {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = totalAmount,
                    onValueChange = { totalAmount = it },
                    label = { Text("Total Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Payer Section
                Text("Paid by", style = MaterialTheme.typography.titleMedium)
                Row {
                    FilterChip(
                        selected = payerSelection == PayerSelection.SINGLE,
                        onClick = { payerSelection = PayerSelection.SINGLE },
                        label = { Text("Single") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = payerSelection == PayerSelection.MULTIPLE,
                        onClick = { payerSelection = PayerSelection.MULTIPLE },
                        label = { Text("Multiple") }
                    )
                }
                if (payerSelection == PayerSelection.SINGLE) {
                    ExposedDropdownMenuBox(
                        expanded = false,
                        onExpandedChange = {}
                    ) {
                        OutlinedTextField(
                            value = singlePayer,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Payer") },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = false,
                            onDismissRequest = { }
                        ) {
                            groupMembers.forEach { member ->
                                DropdownMenuItem(
                                    text = { Text(member) },
                                    onClick = { singlePayer = member }
                                )
                            }
                        }
                    }
                } else {
                    groupMembers.forEach { member ->
                        OutlinedTextField(
                            value = multiplePayers[member] ?: "",
                            onValueChange = { amount ->
                                multiplePayers = multiplePayers.toMutableMap().apply { put(member, amount) }
                            },
                            label = { Text(member) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Split Section
                Text("Split", style = MaterialTheme.typography.titleMedium)
                Row {
                    FilterChip(
                        selected = splitMethod == SplitMethod.EQUALLY,
                        onClick = { splitMethod = SplitMethod.EQUALLY },
                        label = { Text("Equally") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = splitMethod == SplitMethod.UNEQUALLY,
                        onClick = { splitMethod = SplitMethod.UNEQUALLY },
                        label = { Text("Unequally") }
                    )
                }
                if (splitMethod == SplitMethod.UNEQUALLY) {
                    groupMembers.forEach { member ->
                        OutlinedTextField(
                            value = unequalShares[member] ?: "",
                            onValueChange = { amount ->
                                unequalShares = unequalShares.toMutableMap().apply { put(member, amount) }
                            },
                            label = { Text(member) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val total = totalAmount.toDoubleOrNull()
                if (description.isBlank() || total == null) {
                    errorMessage = "Description and amount are required."
                    return@Button
                }

                val payers = if (payerSelection == PayerSelection.SINGLE) {
                    mapOf(singlePayer to total)
                } else {
                    multiplePayers.mapValues { it.value.toDoubleOrNull() ?: 0.0 }
                }

                if (payers.values.sum() != total) {
                    errorMessage = "Payer amounts must sum to the total."
                    return@Button
                }

                val shares = if (splitMethod == SplitMethod.EQUALLY) {
                    groupMembers.associateWith { total / groupMembers.size }
                } else {
                    unequalShares.mapValues { it.value.toDoubleOrNull() ?: 0.0 }
                }

                if (shares.values.sum() != total) {
                    errorMessage = "Shares must sum to the total."
                    return@Button
                }

                onAddExpense(Expense(description, total, payers, shares))
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}