package cz.splnsito.mrthom.loglantern.feature.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.splnsito.mrthom.loglantern.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SplunkAuthUiState(
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val error: String? = null,
    val createdToken: String? = null,
    val tokenExpiry: String? = null
)

@HiltViewModel
class SplunkAuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplunkAuthUiState())
    val uiState = _uiState.asStateFlow()

    fun login(baseUrl: String, username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            loginUseCase(baseUrl, username, password)
                .onSuccess { tokenInfo ->
                    Log.d("SplunkAuthViewModel", "Token created successfully")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loginSuccess = true,
                            createdToken = tokenInfo.token,
                            tokenExpiry = tokenInfo.expiry
                        )
                    }
                }
                .onFailure { throwable ->
                    Log.e("SplunkAuthViewModel", "Login failed", throwable)
                    _uiState.update { it.copy(isLoading = false, error = throwable.message) }
                }
        }
    }

    fun acknowledgeToken() {
        _uiState.update { it.copy(createdToken = null, tokenExpiry = null) }
    }
}
