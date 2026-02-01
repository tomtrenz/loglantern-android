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
    }

    val baseUrl: Flow<String?> = dataStore.data.map { preferences ->
        preferences[BASE_URL_KEY]
    }

    suspend fun saveBaseUrl(url: String) {
        dataStore.edit { settings ->
            settings[BASE_URL_KEY] = url
        }
    }
}
