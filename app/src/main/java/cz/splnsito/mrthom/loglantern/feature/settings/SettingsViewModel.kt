package cz.splnsito.mrthom.loglantern.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.splnsito.mrthom.loglantern.core.security.TokenStorage
import cz.splnsito.mrthom.loglantern.data.local.SettingsDataStore
import cz.splnsito.mrthom.loglantern.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val baseUrl: String? = null,
    val username: String? = null,
    val token: String? = null,
    val tokenExpiry: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val settingsDataStore: SettingsDataStore,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                settingsDataStore.baseUrl,
                settingsDataStore.username,
                tokenStorage.getToken(),
                settingsDataStore.tokenExpiry
            ) { baseUrl, username, token, tokenExpiry ->
                SettingsUiState(
                    baseUrl = baseUrl,
                    username = username,
                    token = token,
                    tokenExpiry = tokenExpiry
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            authRepository.clearData()
            onLogout()
        }
    }
}
