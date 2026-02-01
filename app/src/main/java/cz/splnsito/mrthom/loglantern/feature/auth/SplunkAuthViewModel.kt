package cz.splnsito.mrthom.loglantern.feature.auth

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

data class SplunkAuthUiState(
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false
)

@HiltViewModel
class SplunkAuthViewModel @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplunkAuthUiState())
    val uiState = _uiState.asStateFlow()

    fun login(baseUrl: String, username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Fake login
            settingsDataStore.saveBaseUrl(baseUrl)
            tokenStorage.saveToken("dummy-token-for-$username")
            _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
        }
    }
}
