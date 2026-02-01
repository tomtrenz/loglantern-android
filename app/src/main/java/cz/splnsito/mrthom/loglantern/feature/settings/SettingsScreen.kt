package cz.splnsito.mrthom.loglantern.feature.settings

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
fun SettingsScreen(
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Settings",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(24.dp))

            Text(
                text = "Base URL: ${uiState.baseUrl ?: "Not set"}",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Username: ${uiState.username ?: "Not set"}",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Token: ${uiState.token?.take(20) ?: "Not set"}${if (uiState.token != null && uiState.token!!.length > 20) "..." else ""}",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Token Expiry: ${uiState.tokenExpiry ?: "Not set"}",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(32.dp))
            PrimaryButton(
                text = "Logout",
                onClick = { viewModel.logout(onLogout) }
            )
        }
    }
}
