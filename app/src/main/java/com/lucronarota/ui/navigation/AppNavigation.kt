package com.lucronarota.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lucronarota.ui.screens.jornada.JornadaScreen
import com.lucronarota.ui.screens.custos.CustosScreen
import com.lucronarota.ui.screens.dashboard.DashboardScreen
import com.lucronarota.ui.screens.distribuicao.DistribuicaoScreen
import com.lucronarota.ui.screens.metas.MetasScreen
import com.lucronarota.ui.screens.home.HomeScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Inicio", Icons.Filled.Home)
    data object Jornada : Screen("jornada", "Jornada", Icons.Filled.Timer)
    data object Custos : Screen("custos", "Custos", Icons.Filled.AttachMoney)
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.BarChart)
    data object Distribuicao : Screen("distribuicao", "Distribuicao", Icons.Filled.PieChart)
    data object Metas : Screen("metas", "Metas", Icons.Filled.TrackChanges)
}

val bottomScreens = listOf(
    Screen.Home,
    Screen.Jornada,
    Screen.Custos,
    Screen.Dashboard,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomScreens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Jornada.route) { JornadaScreen() }
            composable(Screen.Custos.route) { CustosScreen() }
            composable(Screen.Dashboard.route) { DashboardScreen() }
            composable(Screen.Distribuicao.route) { DistribuicaoScreen() }
            composable(Screen.Metas.route) { MetasScreen() }
        }
    }
}
