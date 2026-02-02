package cz.splnsito.mrthom.loglantern.feature.results.chart

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.splnsito.mrthom.loglantern.domain.usecase.ExecuteSearchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChartDataPoint(
    val label: String,
    val value: Float
)

data class ResultsPieChartUiState(
    val searchQuery: String = "search index=netflow | head 10000 | stats count by src_addr",
    val chartData: List<ChartDataPoint> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val statusMessage: String? = null
)

@HiltViewModel
class ResultsPieChartViewModel @Inject constructor(
    private val executeSearchUseCase: ExecuteSearchUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultsPieChartUiState())
    val uiState = _uiState.asStateFlow()

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun executeSearch() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                statusMessage = "Vytvářím search job..."
            )

            Log.d("PieChartViewModel", "Executing search: ${_uiState.value.searchQuery}")

            try {
                val result = executeSearchUseCase(
                    searchQuery = _uiState.value.searchQuery,
                    maxWaitTime = 60000,
                    resultCount = 0
                )

                result.onSuccess { searchResults ->
                    Log.d("PieChartViewModel", "Search completed with ${searchResults.results.size} results")

                    val chartData = parseStatsResults(searchResults.results)

                    _uiState.value = _uiState.value.copy(
                        chartData = chartData,
                        isLoading = false,
                        statusMessage = "Načteno ${chartData.size} hodnot",
                        errorMessage = null
                    )
                }.onFailure { error ->
                    Log.e("PieChartViewModel", "Search failed", error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Chyba: ${error.message}",
                        statusMessage = null
                    )
                }
            } catch (e: Exception) {
                Log.e("PieChartViewModel", "Unexpected error", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Neočekávaná chyba: ${e.message}",
                    statusMessage = null
                )
            }
        }
    }

    private fun parseStatsResults(results: List<Map<String, Any>>): List<ChartDataPoint> {
        val dataPoints = mutableListOf<ChartDataPoint>()

        results.forEach { result ->
            // Najdi klíč, který není "count" (to je label)
            val labelKey = result.keys.firstOrNull { it != "count" }
            val label = labelKey?.let { result[it]?.toString() } ?: "Neznámý"

            // Získej hodnotu "count"
            val countValue = result["count"]
            val count = when (countValue) {
                is Number -> countValue.toFloat()
                is String -> countValue.toFloatOrNull() ?: 0f
                else -> 0f
            }

            if (count > 0) {
                dataPoints.add(ChartDataPoint(label, count))
                Log.d("PieChartViewModel", "Parsed data point: $label = $count")
            }
        }

        // Seřaď od největší hodnoty
        return dataPoints.sortedByDescending { it.value }
    }
}
