package cz.splnsito.mrthom.loglantern.feature.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.splnsito.mrthom.loglantern.core.ui.components.LoadingView

@Composable
fun SplashScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.destination) {
        when (uiState.destination) {
            SplashUiState.Destination.DASHBOARD -> onNavigateToDashboard()
            SplashUiState.Destination.LOGIN -> onNavigateToLogin()
            null -> { /* Wait for destination */ }
        }
    }

    LoadingView()
}
