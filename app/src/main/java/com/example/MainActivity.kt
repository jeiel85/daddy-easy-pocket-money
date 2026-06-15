package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.AppViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    // Request permissions launcher for system notifications (Android 13+)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result handled gracefully by system
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val viewModel: AppViewModel = viewModel()

                // Capture current navigation state to selectively hide bottom navigation bars
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val showBottomBar = currentRoute != null && !currentRoute.startsWith("input_expense")

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                // Home Tab
                                NavigationBarItem(
                                    selected = currentRoute == "home",
                                    onClick = {
                                        navController.navigate("home") {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "홈", modifier = Modifier.size(26.dp)) },
                                    label = { Text("홈", fontSize = 13.sp) }
                                )

                                // History Tab
                                NavigationBarItem(
                                    selected = currentRoute == "history",
                                    onClick = {
                                        navController.navigate("history") {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "지출대장", modifier = Modifier.size(26.dp)) },
                                    label = { Text("지출대장", fontSize = 13.sp) }
                                )

                                // Fixed Bills Tab
                                NavigationBarItem(
                                    selected = currentRoute == "fixed",
                                    onClick = {
                                        navController.navigate("fixed") {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "고정비", modifier = Modifier.size(26.dp)) },
                                    label = { Text("고정비", fontSize = 13.sp) }
                                )

                                // Cards Tab
                                NavigationBarItem(
                                    selected = currentRoute == "cards",
                                    onClick = {
                                        navController.navigate("cards") {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.CreditCard, contentDescription = "카드정리", modifier = Modifier.size(26.dp)) },
                                    label = { Text("카드정리", fontSize = 13.sp) }
                                )

                                // Settings / Stats Tab
                                NavigationBarItem(
                                    selected = currentRoute == "settings",
                                    onClick = {
                                        navController.navigate("settings") {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "분석/설정", modifier = Modifier.size(26.dp)) },
                                    label = { Text("설정/분석", fontSize = 13.sp) }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToInput = { id ->
                                    navController.navigate("input_expense?id=$id")
                                },
                                onNavigateToStats = {
                                    navController.navigate("settings")
                                }
                            )
                        }

                        composable("history") {
                            HistoryScreen(
                                viewModel = viewModel,
                                onNavigateToEdit = { id ->
                                    navController.navigate("input_expense?id=$id")
                                }
                            )
                        }

                        composable("fixed") {
                            FixedExpensesScreen(
                                viewModel = viewModel
                            )
                        }

                        composable("cards") {
                            CardsScreen(
                                viewModel = viewModel
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel
                            )
                        }

                        composable(
                            route = "input_expense?id={id}",
                            arguments = listOf(
                                navArgument("id") {
                                    type = NavType.IntType
                                    defaultValue = 0
                                }
                            )
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getInt("id") ?: 0
                            InputExpenseScreen(
                                viewModel = viewModel,
                                expenseId = id,
                                onNavigateBack = {
                                    navController.navigateUp()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
