package cz.splnsito.mrthom.loglantern.feature.results.table

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.splnsito.mrthom.loglantern.core.ui.components.ErrorView
import cz.splnsito.mrthom.loglantern.core.ui.components.LoadingView
import cz.splnsito.mrthom.loglantern.core.ui.components.PrimaryButton

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
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Search Results Table",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Search Input
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("SPL Query") },
                placeholder = { Text("search index=main | head 100") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                singleLine = false,
                minLines = 2,
                maxLines = 4
            )

            Spacer(Modifier.height(8.dp))

            // Execute Button
            PrimaryButton(
                text = if (uiState.isLoading) "Searching..." else "Execute Search",
                onClick = { viewModel.executeSearch() },
                enabled = !uiState.isLoading && uiState.searchQuery.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Content Area
            when {
                uiState.isLoading -> {
                    LoadingView(
                        text = "Executing search job...\nThis may take a few seconds"
                    )
                }

                uiState.error != null -> {
                    ErrorView(
                        message = uiState.error ?: "Unknown error",
                        onRetry = { viewModel.executeSearch() }
                    )
                }

                uiState.searchExecuted && uiState.rows.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No results found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                uiState.rows.isNotEmpty() -> {
                    // Results Summary
                    Text(
                        text = "Results: ${uiState.rows.size} rows, ${uiState.columns.size} columns",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Scrollable Table
                    DataTable(
                        columns = uiState.columns,
                        rows = uiState.rows,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    // Initial state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Enter a search query and click Execute Search",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DataTable(
    columns: List<String>,
    rows: List<Map<String, Any>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
    ) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .horizontalScroll(rememberScrollState())
        ) {
            columns.forEach { column ->
                TableCell(
                    text = column,
                    isHeader = true,
                    modifier = Modifier.width(200.dp)
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        // Data Rows
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            rows.forEachIndexed { rowIndex, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (rowIndex % 2 == 0) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .horizontalScroll(rememberScrollState())
                ) {
                    columns.forEach { column ->
                        val value = row[column]?.toString() ?: ""
                        TableCell(
                            text = value,
                            isHeader = false,
                            modifier = Modifier.width(200.dp)
                        )
                    }
                }

                if (rowIndex < rows.size - 1) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
fun TableCell(
    text: String,
    isHeader: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .padding(12.dp),
        style = if (isHeader) {
            MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        } else {
            MaterialTheme.typography.bodyMedium
        },
        color = if (isHeader) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    )
}
