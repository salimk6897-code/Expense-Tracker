package com.example.expensetracker.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.data.Currency
import com.example.expensetracker.data.GroupEntity
import com.example.expensetracker.data.MemberEntity
import com.example.expensetracker.data.SplitExpenseEntity
import com.example.expensetracker.ui.components.AddSplitExpenseDialog
import com.example.expensetracker.ui.theme.PrimaryGreen
import com.example.expensetracker.util.Transaction
import com.example.expensetracker.viewmodel.SplitExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitExpenseScreen(
    modifier: Modifier = Modifier,
    viewModel: SplitExpenseViewModel
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("ExpenseTracker", Context.MODE_PRIVATE)
    
    val groups by viewModel.allGroups.collectAsStateWithLifecycle()
    val members by viewModel.allMembers.collectAsStateWithLifecycle()
    val selectedGroupId by viewModel.selectedGroupId.collectAsStateWithLifecycle()
    val expenses by viewModel.currentGroupExpenses.collectAsStateWithLifecycle()
    val settlements by viewModel.settlements.collectAsStateWithLifecycle()
    val selectedCurrency by viewModel.selectedCurrency.collectAsStateWithLifecycle()

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showCurrencyMenu by remember { mutableStateOf(false) }
    var selectedExpenseDetails by remember { mutableStateOf<SplitExpenseEntity?>(null) }
    
    var showDeleteGroupConfirm by remember { mutableStateOf<Long?>(null) }
    var showDeleteExpenseConfirm by remember { mutableStateOf<Long?>(null) }

    // Sync currency from SharedPreferences
    LaunchedEffect(Unit) {
        val savedCurrency = sharedPreferences.getString("selectedCurrency", "INR") ?: "INR"
        viewModel.setCurrency(Currency.valueOf(savedCurrency))
        viewModel.addMember("You")
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("SPLIT & SETTLE", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp)) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
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
                                        viewModel.setCurrency(currency)
                                        sharedPreferences.edit().putString("selectedCurrency", currency.name).apply()
                                        showCurrencyMenu = false
                                    }
                                )
                            }
                        }
                    }
                    IconButton(onClick = { showAddMemberDialog = true }) {
                        Icon(Icons.Default.PersonAdd, "Add Member", tint = PrimaryGreen)
                    }
                    if (selectedGroupId != null) {
                        IconButton(onClick = { showDeleteGroupConfirm = selectedGroupId }) {
                            Icon(Icons.Default.DeleteForever, "Delete Group", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedGroupId != null && members.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showAddExpenseDialog = true },
                    containerColor = PrimaryGreen,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, "Add Expense")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            GroupSelector(
                groups = groups,
                selectedId = selectedGroupId,
                onSelect = { viewModel.selectGroup(it) },
                onCreateNew = { showCreateGroupDialog = true }
            )

            Spacer(Modifier.height(24.dp))

            if (selectedGroupId == null) {
                EmptyStateView("Select or create a group to start splitting!")
            } else {
                if (settlements.isNotEmpty()) {
                    SettlementSummaryCard(settlements, selectedCurrency)
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    "Group Expenses",
                    modifier = Modifier.padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(Modifier.height(16.dp))

                if (expenses.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No expenses in this group yet.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(expenses) { expense ->
                            PremiumSplitExpenseItem(
                                expense = expense,
                                currency = selectedCurrency,
                                onClick = { selectedExpenseDetails = expense },
                                onDelete = { showDeleteExpenseConfirm = expense.id }
                            )
                        }
                    }
                }
            }
        }
    }

    // BREAKDOWN DIALOG
    selectedExpenseDetails?.let { expense ->
        ExpenseDetailsDialog(
            expense = expense,
            currency = selectedCurrency,
            onDismiss = { selectedExpenseDetails = null }
        )
    }

    if (showAddMemberDialog) {
        AddMemberDialog(
            onDismiss = { showAddMemberDialog = false },
            onConfirm = { name ->
                viewModel.addMember(name)
                showAddMemberDialog = false
            }
        )
    }

    if (showAddExpenseDialog) {
        AddSplitExpenseDialog(
            members = members.map { it.name },
            onDismiss = { showAddExpenseDialog = false },
            onSave = { expenseEntity ->
                viewModel.addExpenseEntity(expenseEntity)
                showAddExpenseDialog = false
            }
        )
    }

    if (showCreateGroupDialog) {
        CreateGroupDialog(
            membersList = members.map { it.name },
            onDismiss = { showCreateGroupDialog = false },
            onConfirm = { name, desc, selectedMembers ->
                viewModel.createGroup(name, desc, selectedMembers)
                showCreateGroupDialog = false
            },
            onAddNewMember = { name -> viewModel.addMember(name) }
        )
    }

    showDeleteGroupConfirm?.let { groupId ->
        AlertDialog(
            onDismissRequest = { showDeleteGroupConfirm = null },
            title = { Text("Delete Group?") },
            text = { Text("Are you sure you want to delete this group and all its expenses? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGroup(groupId)
                        showDeleteGroupConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteGroupConfirm = null }) { Text("Cancel") }
            }
        )
    }

    showDeleteExpenseConfirm?.let { expenseId ->
        AlertDialog(
            onDismissRequest = { showDeleteExpenseConfirm = null },
            title = { Text("Delete Expense?") },
            text = { Text("Are you sure you want to delete this expense?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExpense(expenseId)
                        showDeleteExpenseConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteExpenseConfirm = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ExpenseDetailsDialog(
    expense: SplitExpenseEntity,
    currency: Currency,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Column {
                Text(expense.description, fontWeight = FontWeight.Bold)
                Text(
                    "${currency.symbol}${String.format("%.2f", expense.totalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryGreen
                )
            }
        },
        text = {
            LazyColumn {
                item {
                    Text("Paid by", fontWeight = FontWeight.Bold, color = PrimaryGreen)
                    Spacer(Modifier.height(8.dp))
                }
                items(expense.payers.toList().filter { it.second > 0 }) { (name, amount) ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(name)
                        Text("${currency.symbol}${String.format("%.2f", amount)}")
                    }
                }
                item {
                    Spacer(Modifier.height(16.dp))
                    Text("Shares", fontWeight = FontWeight.Bold, color = PrimaryGreen)
                    Spacer(Modifier.height(8.dp))
                }
                items(expense.shares.toList().filter { it.second > 0 }) { (name, amount) ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(name)
                        Text("${currency.symbol}${String.format("%.2f", amount)}")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun AddMemberDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Friend", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Friend's Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) { Text("Add Friend") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSelector(
    groups: List<GroupEntity>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    onCreateNew: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCreateNew) {
            Icon(Icons.Default.GroupAdd, "New Group", tint = PrimaryGreen)
        }
        
        Spacer(Modifier.width(8.dp))

        LazyColumn(modifier = Modifier.weight(1f).height(48.dp)) {
            item {
                Row {
                    groups.forEach { group ->
                        FilterChip(
                            selected = group.id == selectedId,
                            onClick = { onSelect(group.id) },
                            label = { Text(group.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CreateGroupDialog(
    membersList: List<String>,
    onDismiss: () -> Unit, 
    onConfirm: (String, String, List<String>) -> Unit,
    onAddNewMember: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedMembers by remember { mutableStateOf(setOf<String>("You")) }
    var newMemberName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Group", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Group Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc, 
                    onValueChange = { desc = it }, 
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                
                Text("Select Members", fontWeight = FontWeight.SemiBold)
                
                Spacer(Modifier.height(8.dp))
                
                // Quick Add Member Inline
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newMemberName,
                        onValueChange = { newMemberName = it },
                        label = { Text("Quick Add Name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(onClick = {
                        if (newMemberName.isNotBlank()) {
                            onAddNewMember(newMemberName)
                            selectedMembers = selectedMembers + newMemberName
                            newMemberName = ""
                        }
                    }) {
                        Icon(Icons.Default.PersonAdd, null, tint = PrimaryGreen)
                    }
                }

                Spacer(Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(membersList) { member ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable {
                                if (member != "You") {
                                    selectedMembers = if (member in selectedMembers) selectedMembers - member else selectedMembers + member
                                }
                            }
                        ) {
                            Checkbox(
                                checked = member in selectedMembers,
                                onCheckedChange = { checked ->
                                    if (member != "You") {
                                        selectedMembers = if (checked) selectedMembers + member else selectedMembers - member
                                    }
                                }
                            )
                            Text(member)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name, desc, selectedMembers.toList()) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun SettlementSummaryCard(settlements: List<Transaction>, currency: Currency) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = PrimaryGreen)
                Spacer(Modifier.width(8.dp))
                Text("Settlement Plan", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            settlements.take(3).forEach { transaction ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${transaction.from} pays ${transaction.to}", fontSize = 14.sp)
                    Text("${currency.symbol}${String.format("%.2f", transaction.amount)}", fontWeight = FontWeight.Bold, color = PrimaryGreen)
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun PremiumSplitExpenseItem(
    expense: SplitExpenseEntity, 
    currency: Currency,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, tint = Color.Gray)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.description, fontWeight = FontWeight.SemiBold)
                Text("${expense.payers.filterValues { it > 0 }.size} payers", fontSize = 12.sp, color = Color.Gray)
            }
            Text("${currency.symbol}${String.format("%.2f", expense.totalAmount)}", fontWeight = FontWeight.Bold)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, "Delete", tint = Color.Gray)
            }
        }
    }
}

@Composable
fun EmptyStateView(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Group, null, modifier = Modifier.size(80.dp), tint = Color.Gray.copy(alpha = 0.3f))
        Spacer(Modifier.height(16.dp))
        Text(message, color = Color.Gray)
    }
}
