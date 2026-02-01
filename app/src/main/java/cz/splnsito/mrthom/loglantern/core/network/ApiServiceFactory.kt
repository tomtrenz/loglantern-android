package cz.splnsito.mrthom.loglantern.core.network

import cz.splnsito.mrthom.loglantern.data.local.SettingsDataStore
import kotlinx.coroutines.flow.first
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Provider

class ApiServiceFactory @Inject constructor(
    private val retrofitBuilder: Provider<Retrofit.Builder>,
    private val settingsDataStore: SettingsDataStore
) {
    suspend fun create(): SplunkApiService {
        val baseUrl = settingsDataStore.baseUrl.first() ?: "https://127.0.0.1/"
        return retrofitBuilder.get()
            .baseUrl(baseUrl)
            .build()
            .create(SplunkApiService::class.java)
    }
}
