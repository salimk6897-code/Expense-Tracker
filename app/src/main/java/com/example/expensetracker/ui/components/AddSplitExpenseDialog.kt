package com.example.expensetracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.expensetracker.data.SplitExpenseEntity
import java.text.DecimalFormat
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSplitExpenseDialog(
    members: List<String>,
    onDismiss: () -> Unit,
    onSave: (SplitExpenseEntity) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var totalAmount by remember { mutableStateOf("") }

    var includedMembers by remember { mutableStateOf(members.toSet()) }

    var paidByMode by remember { mutableStateOf("single") } // single / multiple
    var singlePayer by remember { mutableStateOf(members.firstOrNull() ?: "") }
    var multiplePayers by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    var splitMode by remember { mutableStateOf("equally") } // equally / unequally
    var unequalShares by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                item {
                    Text("Add Split Expense", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = totalAmount,
                        onValueChange = { totalAmount = it },
                        label = { Text("Total Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))
                    Text("Included Members", style = MaterialTheme.typography.titleMedium)
                    members.forEach { member ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = member in includedMembers,
                                onCheckedChange = { checked ->
                                    includedMembers = if (checked) includedMembers + member else includedMembers - member
                                }
                            )
                            Text(member)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("Paid By", style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = paidByMode == "single", onClick = { paidByMode = "single" })
                        Text("Single")
                        Spacer(Modifier.width(12.dp))
                        RadioButton(selected = paidByMode == "multiple", onClick = { paidByMode = "multiple" })
                        Text("Multiple")
                    }

                    if (paidByMode == "single") {
                        PayerDropdown(
                            members = members,
                            selectedPayer = singlePayer,
                            onPayerSelected = { singlePayer = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        includedMembers.forEach { member ->
                            OutlinedTextField(
                                value = multiplePayers[member] ?: "",
                                onValueChange = { v -> multiplePayers = multiplePayers + (member to v) },
                                label = { Text("Paid by $member") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("Split", style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = splitMode == "equally", onClick = { splitMode = "equally" })
                        Text("Equally")
                        Spacer(Modifier.width(12.dp))
                        RadioButton(selected = splitMode == "unequally", onClick = { splitMode = "unequally" })
                        Text("Unequally")
                    }

                    if (splitMode == "unequally") {
                        includedMembers.forEach { member ->
                            OutlinedTextField(
                                value = unequalShares[member] ?: "",
                                onValueChange = { v -> unequalShares = unequalShares + (member to v) },
                                label = { Text("Share for $member") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            val total = totalAmount.toDoubleOrNull()
                            if (description.isBlank() || total == null || total <= 0) {
                                error = "Please enter valid description & amount."
                                return@Button
                            }
                            if (includedMembers.isEmpty()) {
                                error = "Select at least one member."
                                return@Button
                            }

                            val finalPayers: Map<String, Double> = if (paidByMode == "single") {
                                mapOf(singlePayer to total)
                            } else {
                                includedMembers.associateWith { (multiplePayers[it]?.toDoubleOrNull() ?: 0.0) }
                            }

                            if (abs(finalPayers.values.sum() - total) > 0.01) {
                                error = "Payments total must equal ₹$total"
                                return@Button
                            }

                            val finalShares: Map<String, Double> = if (splitMode == "equally") {
                                val each = total / includedMembers.size
                                includedMembers.associateWith { each }
                            } else {
                                includedMembers.associateWith { (unequalShares[it]?.toDoubleOrNull() ?: 0.0) }
                            }

                            if (abs(finalShares.values.sum() - total) > 0.01) {
                                error = "Shares total must equal ₹$total"
                                return@Button
                            }

                            val payersFull = members.associateWith { finalPayers[it] ?: 0.0 }
                            val sharesFull = members.associateWith { finalShares[it] ?: 0.0 }

                            onSave(
                                SplitExpenseEntity(
                                    description = description.trim(),
                                    totalAmount = total,
                                    payers = payersFull,
                                    shares = sharesFull
                                )
                            )
                        }) { Text("Save") }
                    }
                }
            }
        }
    }
}
