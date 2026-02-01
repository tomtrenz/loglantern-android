package cz.splnsito.mrthom.loglantern.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(context: Context) {

    private val dataStore = context.settingsDataStore

    companion object {
        val BASE_URL_KEY = stringPreferencesKey("splunk_base_url")
        val USERNAME_KEY = stringPreferencesKey("splunk_username")
        val TOKEN_EXPIRY_KEY = stringPreferencesKey("splunk_token_expiry")
    }

    val baseUrl: Flow<String?> = dataStore.data.map { preferences ->
        preferences[BASE_URL_KEY]
    }

    val username: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USERNAME_KEY]
    }

    val tokenExpiry: Flow<String?> = dataStore.data.map { preferences ->
        preferences[TOKEN_EXPIRY_KEY]
    }

    suspend fun saveBaseUrl(url: String) {
        dataStore.edit { settings ->
            settings[BASE_URL_KEY] = url
        }
    }

    suspend fun saveUsername(username: String) {
        dataStore.edit { settings ->
            settings[USERNAME_KEY] = username
        }
    }

    suspend fun saveTokenExpiry(expiry: String) {
        dataStore.edit { settings ->
            settings[TOKEN_EXPIRY_KEY] = expiry
        }
    }

    suspend fun clearAll() {
        dataStore.edit { settings ->
            settings.clear()
        }
    }
}
