package cz.splnsito.mrthom.loglantern.core.security

import kotlinx.coroutines.flow.Flow

interface TokenStorage {
    fun getToken(): Flow<String?>
    suspend fun saveToken(token: String)
    suspend fun clearToken()
}
