package cz.splnsito.mrthom.loglantern.feature.results.table

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

@Composable
fun ResultsTableScreen(
    viewModel: ResultsTableViewModel = hiltViewModel()
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
                text = "Results Table",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Table view will be displayed here",
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
            )
        }
    }
}
