package cz.splnsito.mrthom.loglantern.data.repository

import android.util.Base64
import android.util.Log
import cz.splnsito.mrthom.loglantern.core.network.ApiServiceFactory
import cz.splnsito.mrthom.loglantern.core.security.TokenStorage
import cz.splnsito.mrthom.loglantern.data.local.SettingsDataStore
import cz.splnsito.mrthom.loglantern.domain.repository.AuthRepository
import cz.splnsito.mrthom.loglantern.domain.repository.TokenInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiServiceFactory: ApiServiceFactory,
    private val tokenStorage: TokenStorage,
    private val settingsDataStore: SettingsDataStore
) : AuthRepository {

    override suspend fun createAndSaveToken(baseUrl: String, username: String, password: String): Result<TokenInfo> {
        return try {
            // Nejprve uložíme base URL, aby ApiServiceFactory mohlo vytvořit správný endpoint
            settingsDataStore.saveBaseUrl(baseUrl)

            // Vytvoříme API service s novou base URL
            val splunkApiService = apiServiceFactory.create()

            // Vytvoříme Basic Auth header
            val authHeader = "Basic " + Base64.encodeToString(
                "$username:$password".toByteArray(),
                Base64.NO_WRAP
            )

            Log.d("AuthRepositoryImpl", "Creating new token for user: $username")

            // Vytvoříme nový token pomocí POST
            val response = splunkApiService.createToken(
                authHeader = authHeader,
                username = username,
                audience = "LogParser",
                type = "static"
            )

            Log.d("AuthRepositoryImpl", "Received ${response.entry.size} token entries in response")

            // Získáme vytvořený token z odpovědi
            val createdToken = response.entry.firstOrNull()

            if (createdToken == null) {
                Log.e("AuthRepositoryImpl", "No token in response")
                return Result.failure(Exception("Failed to create token for user $username"))
            }

            // Token je přímo v content objektu
            val tokenValue = createdToken.content?.token
            if (tokenValue.isNullOrBlank()) {
                Log.e("AuthRepositoryImpl", "Token value not found in response")
                return Result.failure(Exception("Token value not found in response"))
            }

            // ID tokenu je také přímo v content
            val tokenId = createdToken.content?.id
            Log.d("AuthRepositoryImpl", "Token ID: $tokenId")

            // Dekódujeme JWT token pro získání expiry
            val expiryString = try {
                // JWT je ve formátu header.payload.signature
                val parts = tokenValue.split(".")
                if (parts.size >= 2) {
                    val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
                    // Pokusíme se najít exp v JSON payload
                    val expMatch = Regex("\"exp\":(\\d+)").find(payload)
                    if (expMatch != null) {
                        val exp = expMatch.groupValues[1].toLongOrNull()
                        if (exp != null) {
                            val date = Date(exp * 1000)
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date)
                        } else "Unknown"
                    } else "Unknown"
                } else "Unknown"
            } catch (e: Exception) {
                Log.w("AuthRepositoryImpl", "Failed to decode JWT token expiry", e)
                "Unknown"
            }

            Log.d("AuthRepositoryImpl", "Token created successfully: ${tokenValue.take(20)}..., expires: $expiryString")

            // Uložíme všechna data
            tokenStorage.saveToken(tokenValue)
            settingsDataStore.saveUsername(username)
            settingsDataStore.saveTokenExpiry(expiryString)

            Result.success(TokenInfo(token = tokenValue, expiry = expiryString))
        } catch (e: Exception) {
            Log.e("AuthRepositoryImpl", "Failed to create token", e)
            Result.failure(e)
        }
    }

    override suspend fun clearData() {
        tokenStorage.clearToken()
        settingsDataStore.clearAll()
    }
}
