package com.navieat.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.navieat.app.R
import com.navieat.app.ui.screens.home.HomeScreen
import com.navieat.app.ui.screens.plan.PlanScreen
import com.navieat.app.ui.screens.settings.SettingsScreen
import com.navieat.app.ui.screens.shopping.ShoppingScreen

private sealed class TopDestination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
) {
    data object Home : TopDestination("home", R.string.nav_home, Icons.Default.Restaurant)
    data object Plan : TopDestination("plan", R.string.nav_plan, Icons.Default.CalendarMonth)
    data object Shopping : TopDestination("shopping", R.string.nav_shopping, Icons.Default.ShoppingCart)
    data object Settings : TopDestination("settings", R.string.nav_settings, Icons.Default.Settings)
}

private val topDestinations = listOf(
    TopDestination.Home,
    TopDestination.Plan,
    TopDestination.Shopping,
    TopDestination.Settings,
)

@Composable
fun NaviEatApp() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                topDestinations.forEach { dest ->
                    val selected = backStack?.destination?.hierarchy?.any { it.route == dest.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            nav.navigate(dest.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = null) },
                        label = { Text(stringResource(dest.labelRes)) },
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = TopDestination.Home.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(TopDestination.Home.route) { HomeScreen() }
            composable(TopDestination.Plan.route) { PlanScreen() }
            composable(TopDestination.Shopping.route) { ShoppingScreen() }
            composable(TopDestination.Settings.route) { SettingsScreen() }
        }
    }
}
