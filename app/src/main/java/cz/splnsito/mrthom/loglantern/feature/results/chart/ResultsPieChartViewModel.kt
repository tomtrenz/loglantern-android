package cz.splnsito.mrthom.loglantern.feature.results.chart

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class ResultsPieChartUiState(
    val isLoading: Boolean = false
)

@HiltViewModel
class ResultsPieChartViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ResultsPieChartUiState())
    val uiState = _uiState.asStateFlow()
}
