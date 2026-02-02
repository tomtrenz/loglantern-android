package cz.splnsito.mrthom.loglantern.feature.results.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ResultsPieChartScreen(
    viewModel: ResultsPieChartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "📊 Splunk Statistiky",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Search Query Input
        OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("Search dotaz (se stats)") },
                placeholder = { Text("search index=netflow | head 10000 | stats count by src_addr") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Search Button
            Button(
                onClick = { viewModel.executeSearch() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && uiState.searchQuery.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Načítám...")
                } else {
                    Text("🔍 Spustit vyhledávání")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status/Error Messages
            uiState.statusMessage?.let { status ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "ℹ️ $status",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            uiState.errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "❌ $error",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Chart
            if (uiState.chartData.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Koláčový graf statistik",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        PieChart(data = uiState.chartData)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Data Summary
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📋 Detaily (${uiState.chartData.size} položek)",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        uiState.chartData.take(10).forEach { dataPoint ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = dataPoint.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = dataPoint.value.toInt().toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                            }
                            if (dataPoint != uiState.chartData.take(10).last()) {
                                HorizontalDivider()
                            }
                        }

                        if (uiState.chartData.size > 10) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "... a ${uiState.chartData.size - 10} dalších",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            } else if (!uiState.isLoading && uiState.errorMessage == null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📈",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Zadejte SPL dotaz se stats",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Příklad:\nsearch index=netflow | head 10000 | stats count by src_addr",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }


@Composable
private fun PieChart(data: List<ChartDataPoint>) {
    // Připrav data - vezmi top 8 položek, zbytek jako "Ostatní"
    val topData = data.take(8)
    val otherSum = data.drop(8).sumOf { it.value.toDouble() }.toFloat()

    val chartData = if (otherSum > 0) {
        topData + ChartDataPoint("Ostatní", otherSum)
    } else {
        topData
    }

    val total = chartData.sumOf { it.value.toDouble() }.toFloat()

    // Barvy pro koláčový graf
    val colors = listOf(
        Color(0xFF6200EE), // Primary Purple
        Color(0xFF03DAC5), // Teal
        Color(0xFFFF6F00), // Orange
        Color(0xFF018786), // Dark Teal
        Color(0xFFB00020), // Red
        Color(0xFF3700B3), // Dark Purple
        Color(0xFFFFA726), // Light Orange
        Color(0xFF26A69A), // Light Teal
        Color(0xFF9E9E9E)  // Gray for "Ostatní"
    )

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Koláčový graf (Canvas)
        Canvas(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
                .padding(8.dp)
        ) {
            val canvasSize = size.minDimension
            val radius = canvasSize / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            var startAngle = -90f // Start from top

            chartData.forEachIndexed { index, dataPoint ->
                val sweepAngle = (dataPoint.value / total) * 360f
                val color = colors[index % colors.size]

                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(
                        center.x - radius,
                        center.y - radius
                    ),
                    size = Size(radius * 2, radius * 2)
                )

                startAngle += sweepAngle
            }

            // Bílé střed pro "donut" efekt (volitelné)
            val innerRadius = radius * 0.5f
            drawCircle(
                color = Color.White,
                radius = innerRadius,
                center = center
            )
        }

        // Legenda
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            chartData.forEachIndexed { index, dataPoint ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Barevný čtvereček
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                colors[index % colors.size],
                                CircleShape
                            )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Label a procenta
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = dataPoint.label.take(15),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                        val percentage = (dataPoint.value / total * 100).toInt()
                        Text(
                            text = "$percentage% (${dataPoint.value.toInt()})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

