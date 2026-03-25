package com.example.expensetracker.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.data.Currency
import com.example.expensetracker.data.SimpleExpense
import com.example.expensetracker.ui.theme.PrimaryGreen
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("ExpenseTracker", Context.MODE_PRIVATE)
    val gson = remember { Gson() }

    var expenses by remember {
        mutableStateOf(
            try {
                val json = sharedPreferences.getString("expensesJson", "[]")
                val type = object : TypeToken<List<SimpleExpense>>() {}.type
                gson.fromJson(json, type) ?: emptyList<SimpleExpense>()
            } catch (e: Exception) {
                emptyList<SimpleExpense>()
            }
        )
    }

    var selectedCurrency by remember {
        mutableStateOf(
            Currency.valueOf(sharedPreferences.getString("selectedCurrency", "INR") ?: "INR")
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<SimpleExpense?>(null) }
    var showClearAllConfirm by remember { mutableStateOf(false) }
    var showCurrencyMenu by remember { mutableStateOf(false) }

    fun saveExpenses(newList: List<SimpleExpense>) {
        val json = gson.toJson(newList)
        sharedPreferences.edit().putString("expensesJson", json).apply()
        expenses = newList
    }

    fun saveCurrency(currency: Currency) {
        sharedPreferences.edit().putString("selectedCurrency", currency.name).apply()
        selectedCurrency = currency
    }

    val totalBalance = expenses.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "POCKET MATE", 
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    ) 
                },
                actions = {
                    Box {
                        IconButton(onClick = { showCurrencyMenu = true }) {
                            Text(selectedCurrency.symbol, fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 20.sp)
                        }
                        DropdownMenu(expanded = showCurrencyMenu, onDismissRequest = { showCurrencyMenu = false }) {
                            Currency.entries.forEach { currency ->
                                DropdownMenuItem(
                                    text = { Text("${currency.symbol} - ${currency.code}") },
                                    onClick = {
                                        saveCurrency(currency)
                                        showCurrencyMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryGreen,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Add")
                Spacer(Modifier.width(8.dp))
                Text("New Expense")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            SummaryCard(totalBalance, selectedCurrency)

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recent Transactions",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                if (expenses.isNotEmpty()) {
                    TextButton(onClick = { showClearAllConfirm = true }) {
                        Text("Clear All", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (expenses.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No transactions yet", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(expenses) { expense ->
                        PremiumExpenseItem(
                            expense = expense,
                            currency = selectedCurrency,
                            onDelete = { expenseToDelete = expense }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSimpleExpenseDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, amount, category ->
                val newExpense = SimpleExpense("$name ($category)", String.format("%.2f", amount))
                saveExpenses(expenses + newExpense)
                showAddDialog = false
            }
        )
    }

    expenseToDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("Delete Expense?") },
            text = { Text("Are you sure you want to delete this expense?") },
            confirmButton = {
                Button(
                    onClick = {
                        val newList = expenses - expense
                        saveExpenses(newList)
                        expenseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) { Text("Cancel") }
            }
        )
    }

    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = { Text("Clear All Expenses?") },
            text = { Text("Are you sure you want to delete all expenses? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        saveExpenses(emptyList())
                        showClearAllConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Clear All") }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AddSimpleExpenseDialog(onDismiss: () -> Unit, onSave: (String, Double, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }
    val categories = listOf("Food", "Travel", "Shopping", "Bill", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Expense Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text("Category", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    categories.drop(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull()
                    if (name.isNotBlank() && amt != null) {
                        onSave(name, amt, category)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("Save Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun SummaryCard(total: Double, currency: Currency) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(PrimaryGreen, Color(0xFF15803D))
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    "Total Spending",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "${currency.symbol}${String.format("%,.2f", total)}",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp
                    )
                )
            }
            Icon(
                Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 20.dp, y = 20.dp),
                tint = Color.White.copy(alpha = 0.15f)
            )
        }
    }
}

@Composable
fun PremiumExpenseItem(expense: SimpleExpense, currency: Currency, onDelete: () -> Unit) {
    val category = expense.name.substringAfter("(").substringBefore(")")
    val name = expense.name.substringBefore(" (")
    
    val icon = when (category) {
        "Food" -> Icons.Default.Restaurant
        "Travel" -> Icons.Default.Flight
        "Shopping" -> Icons.Default.ShoppingBag
        "Bill" -> Icons.Default.Receipt
        else -> Icons.Default.Category
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(PrimaryGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PrimaryGreen)
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(category, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "-${currency.symbol}${expense.amount}",
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 18.sp
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.DeleteOutline, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
