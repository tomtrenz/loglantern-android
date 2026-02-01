package cz.splnsito.mrthom.loglantern.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.splnsito.mrthom.loglantern.core.security.TokenStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SplashUiState(
    val destination: Destination? = null
) {
    enum class Destination {
        DASHBOARD, LOGIN
    }
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState = _uiState.asStateFlow()

    init {
        checkToken()
    }

    private fun checkToken() {
        viewModelScope.launch {
            val token = tokenStorage.getToken().first()
            _uiState.value = if (token.isNullOrBlank()) {
                SplashUiState(destination = SplashUiState.Destination.LOGIN)
            } else {
                SplashUiState(destination = SplashUiState.Destination.DASHBOARD)
            }
        }
    }
}
