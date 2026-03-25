package com.example.expensetracker

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.data.Graph
import com.example.expensetracker.repository.GroupRepository
import com.example.expensetracker.repository.SplitExpenseRepository
import com.example.expensetracker.ui.screens.ExpenseScreen
import com.example.expensetracker.ui.screens.SplitExpenseScreen
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.ui.theme.PrimaryGreen
import com.example.expensetracker.viewmodel.SplitExpenseViewModel

enum class ThemeMode { LIGHT, DARK, SYSTEM }

class MainActivity : ComponentActivity() {

    private val splitExpenseViewModel by viewModels<SplitExpenseViewModel> { 
        SplitExpenseViewModel.provideFactory(
            SplitExpenseRepository(Graph.memberDao, Graph.splitExpenseDao),
            GroupRepository(Graph.groupDao)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val sharedPreferences = context.getSharedPreferences("ExpenseTracker", Context.MODE_PRIVATE)
            
            var themeMode by remember {
                mutableStateOf(ThemeMode.valueOf(sharedPreferences.getString("theme_mode", "SYSTEM") ?: "SYSTEM"))
            }

            var currentScreen by remember { mutableStateOf("EXPENSES") }

            ExpenseTrackerTheme(
                darkTheme = when(themeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                }
            ) {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Home, null) },
                                label = { Text("Expenses") },
                                selected = currentScreen == "EXPENSES",
                                onClick = { currentScreen = "EXPENSES" }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.List, null) },
                                label = { Text("Split") },
                                selected = currentScreen == "SPLIT",
                                onClick = { currentScreen = "SPLIT" }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Settings, null) },
                                label = { Text("Settings") },
                                selected = currentScreen == "SETTINGS",
                                onClick = { currentScreen = "SETTINGS" }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        when (currentScreen) {
                            "EXPENSES" -> ExpenseScreen(modifier = Modifier.fillMaxSize())
                            "SPLIT" -> SplitExpenseScreen(
                                modifier = Modifier.fillMaxSize(),
                                viewModel = splitExpenseViewModel
                            )
                            "SETTINGS" -> SettingsScreen(
                                currentTheme = themeMode,
                                onThemeChange = { mode ->
                                    themeMode = mode
                                    sharedPreferences.edit().putString("theme_mode", mode.name).apply()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(currentTheme: ThemeMode, onThemeChange: (ThemeMode) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            "SETTINGS", 
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        )
        Spacer(Modifier.height(32.dp))
        
        Text("Appearance", fontWeight = FontWeight.Bold, color = PrimaryGreen)
        Spacer(Modifier.height(16.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ThemeOption("Light Mode", Icons.Default.LightMode, currentTheme == ThemeMode.LIGHT) {
                    onThemeChange(ThemeMode.LIGHT)
                }
                ThemeOption("Dark Mode", Icons.Default.DarkMode, currentTheme == ThemeMode.DARK) {
                    onThemeChange(ThemeMode.DARK)
                }
                ThemeOption("System Default", Icons.Default.SettingsSuggest, currentTheme == ThemeMode.SYSTEM) {
                    onThemeChange(ThemeMode.SYSTEM)
                }
            }
        }
    }
}

@Composable
fun ThemeOption(title: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .height(48.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (selected) PrimaryGreen else MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.width(16.dp))
        Text(
            title, 
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(Icons.Default.Check, null, tint = PrimaryGreen)
        }
    }
}
