package cz.splnsito.mrthom.loglantern.feature.results.table

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.splnsito.mrthom.loglantern.domain.usecase.ExecuteSearchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResultsTableUiState(
    val searchQuery: String = "search index=main earliest=-1h | head 100",
    val isLoading: Boolean = false,
    val error: String? = null,
    val columns: List<String> = emptyList(),
    val rows: List<Map<String, Any>> = emptyList(),
    val searchExecuted: Boolean = false
)

@HiltViewModel
class ResultsTableViewModel @Inject constructor(
    private val executeSearchUseCase: ExecuteSearchUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultsTableUiState())
    val uiState = _uiState.asStateFlow()

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun executeSearch() {
        val query = _uiState.value.searchQuery
        if (query.isBlank()) {
            _uiState.update { it.copy(error = "Search query cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            executeSearchUseCase(
                searchQuery = query,
                maxWaitTime = 60000,
                resultCount = 0
            ).onSuccess { results ->
                val columns = results.fields.mapNotNull { it.name }.ifEmpty {
                    // Pokud API nevrátí fields, extrahujeme je z prvního řádku
                    results.results.firstOrNull()?.keys?.toList() ?: emptyList()
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        columns = columns,
                        rows = results.results,
                        searchExecuted = true,
                        error = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Unknown error occurred",
                        searchExecuted = true
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
