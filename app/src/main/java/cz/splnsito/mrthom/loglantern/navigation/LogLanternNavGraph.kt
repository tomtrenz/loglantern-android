package cz.splnsito.mrthom.loglantern.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cz.splnsito.mrthom.loglantern.feature.auth.SplunkLoginScreen
import cz.splnsito.mrthom.loglantern.feature.dashboard.DashboardScreen
import cz.splnsito.mrthom.loglantern.feature.results.chart.ResultsPieChartScreen
import cz.splnsito.mrthom.loglantern.feature.results.table.ResultsTableScreen
import cz.splnsito.mrthom.loglantern.feature.settings.SettingsScreen
import cz.splnsito.mrthom.loglantern.feature.splash.SplashScreen

@Composable
fun LogLanternNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = LogLanternRoutes.SPLASH) {
        composable(LogLanternRoutes.SPLASH) {
            SplashScreen(
                onNavigateToDashboard = {
                    navController.navigate(LogLanternRoutes.DASHBOARD) {
                        popUpTo(LogLanternRoutes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(LogLanternRoutes.LOGIN) {
                        popUpTo(LogLanternRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        composable(LogLanternRoutes.LOGIN) {
            SplunkLoginScreen(
                onLoginSuccess = {
                    navController.navigate(LogLanternRoutes.DASHBOARD) {
                        popUpTo(LogLanternRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(LogLanternRoutes.DASHBOARD) {
            DashboardScreen(
                onNavigateToTable = { navController.navigate(LogLanternRoutes.RESULTS_TABLE) },
                onNavigateToPieChart = { navController.navigate(LogLanternRoutes.RESULTS_PIE_CHART) },
                onNavigateToSettings = { navController.navigate(LogLanternRoutes.SETTINGS) }
            )
        }
        composable(LogLanternRoutes.SETTINGS) {
            SettingsScreen(
                onLogout = {
                    navController.navigate(LogLanternRoutes.LOGIN) {
                        popUpTo(LogLanternRoutes.DASHBOARD) { inclusive = true }
                    }
                }
            )
        }
        composable(LogLanternRoutes.RESULTS_TABLE) {
            ResultsTableScreen()
        }
        composable(LogLanternRoutes.RESULTS_PIE_CHART) {
            ResultsPieChartScreen()
        }
    }
}
