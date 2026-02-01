package cz.splnsito.mrthom.loglantern.feature.results.table

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class ResultsTableUiState(
    val isLoading: Boolean = false
)

@HiltViewModel
class ResultsTableViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ResultsTableUiState())
    val uiState = _uiState.asStateFlow()
}
