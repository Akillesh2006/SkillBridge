package com.skillbridge.ai.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by
preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val JWT_TOKEN_KEY = stringPreferencesKey("jwt_token")
    }

    @Volatile
    private var inMemoryToken: String? = null

    fun getToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            val token = preferences[JWT_TOKEN_KEY]
            inMemoryToken = token
            token
        }
    }

    fun getCachedToken(): String? {
        return inMemoryToken
    }

    suspend fun saveToken(token: String) {
        inMemoryToken = token
        context.dataStore.edit { preferences ->
            preferences[JWT_TOKEN_KEY] = token
        }
    }

    suspend fun deleteToken() {
        inMemoryToken = null
        context.dataStore.edit { preferences ->
            preferences.remove(JWT_TOKEN_KEY)
        }
    }

    suspend fun isTokenAvailable(): Boolean {
        return inMemoryToken != null || getToken().first() != null
    }
}