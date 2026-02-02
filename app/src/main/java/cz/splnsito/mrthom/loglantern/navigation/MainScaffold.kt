package cz.splnsito.mrthom.loglantern.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun MainScaffold(
    navController: NavHostController,
    content: @Composable (NavHostController) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Zobrazit bottom navigation pouze pro hlavní obrazovky (ne splash/login)
    val showBottomBar = currentDestination?.route in listOf(
        LogLanternRoutes.DASHBOARD,
        LogLanternRoutes.RESULTS_TABLE,
        LogLanternRoutes.RESULTS_PIE_CHART,
        LogLanternRoutes.SETTINGS
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    BottomNavItem.items.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.route
                        } == true

                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        // NavHost handles its own padding, so we pass the paddingValues through modifier
        Box(modifier = Modifier.padding(paddingValues)) {
            content(navController)
        }
    }
}
