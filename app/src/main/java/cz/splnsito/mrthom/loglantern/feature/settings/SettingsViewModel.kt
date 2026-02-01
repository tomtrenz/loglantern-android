package cz.splnsito.mrthom.loglantern.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.splnsito.mrthom.loglantern.core.security.TokenStorage
import cz.splnsito.mrthom.loglantern.data.local.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val baseUrl: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsDataStore.baseUrl.collect { url ->
                _uiState.update { it.copy(baseUrl = url) }
            }
        }
    }

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            tokenStorage.clearToken()
            onLogout()
        }
    }
}
