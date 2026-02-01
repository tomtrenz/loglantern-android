package cz.splnsito.mrthom.loglantern.domain.usecase

import cz.splnsito.mrthom.loglantern.domain.repository.AuthRepository
import cz.splnsito.mrthom.loglantern.domain.repository.TokenInfo
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(baseUrl: String, username: String, password: String): Result<TokenInfo> {
        return authRepository.createAndSaveToken(baseUrl, username, password)
    }
}
