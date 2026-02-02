package cz.splnsito.mrthom.loglantern.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Dashboard : BottomNavItem(
        route = LogLanternRoutes.DASHBOARD,
        icon = Icons.Default.Home,
        label = "Domů"
    )

    object Table : BottomNavItem(
        route = LogLanternRoutes.RESULTS_TABLE,
        icon = Icons.Default.TableChart,
        label = "Tabulka"
    )

    object PieChart : BottomNavItem(
        route = LogLanternRoutes.RESULTS_PIE_CHART,
        icon = Icons.Default.PieChart,
        label = "Graf"
    )

    object Settings : BottomNavItem(
        route = LogLanternRoutes.SETTINGS,
        icon = Icons.Default.Settings,
        label = "Nastavení"
    )

    companion object {
        val items = listOf(Dashboard, Table, PieChart, Settings)
    }
}
