package com.example.android.academicachievement.common

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(@ApplicationContext val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("preferencesStore")
        val USER_LOGIN = stringPreferencesKey("login")
        val USER_NAME = stringPreferencesKey("full_name")
        val USER_PIN = stringPreferencesKey("pin")
        val ADMIN_PIN = stringPreferencesKey("admin_pin")
    }

    suspend fun saveLoginData(loginData: LoginData) {
        context.dataStore.edit { preferences ->
            preferences[USER_LOGIN] = loginData.login
            preferences[USER_NAME] = loginData.fullName
            preferences[USER_PIN] = loginData.pin
        }
    }

    suspend fun saveAdminPin(value: String) {
        context.dataStore.edit { preferences ->
            preferences[ADMIN_PIN] = value
        }
    }


    data class LoginData(val login: String, val fullName: String="", val pin: String)

    suspend fun getLoginData(): LoginData {
        val flow = context.dataStore.data.map { preferences ->
            LoginData(
                preferences[USER_LOGIN] ?: "",
                preferences[USER_NAME] ?: "",
                preferences[USER_PIN] ?: ""
            )
        }
        return flow.first()
    }


    suspend fun getAdminPin(): String {
        val flow = context.dataStore.data.map { preferences ->
            preferences[ADMIN_PIN] ?: ""
        }
        return flow.first()
    }
}