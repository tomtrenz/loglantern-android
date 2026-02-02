package cz.splnsito.mrthom.loglantern.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.splnsito.mrthom.loglantern.core.ui.components.PrimaryButton

@Composable
fun DashboardScreen(
    onNavigateToTable: () -> Unit = {},
    onNavigateToPieChart: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🏠 Dashboard",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(32.dp))

        Text(
            text = "Vítejte v LogLantern",
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(16.dp))

        Text(
            text = "Použijte navigační menu níže pro přepínání mezi obrazovkami",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
