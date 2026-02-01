package cz.splnsito.mrthom.loglantern.domain.repository

data class TokenInfo(
    val token: String,
    val expiry: String
)

interface AuthRepository {
    suspend fun createAndSaveToken(baseUrl: String, username: String, password: String): Result<TokenInfo>
    suspend fun clearData()
}
