package com.ikhokha.techcheck.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

const val GUEST_EMAIL = "guest@email.com"

data class LoginDetails(val email: String, val password: String, val uid: String)
data class UserDetails(val name: String, val surname: String, val phone: String, val latitude: Double, val longitude: Double)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_details")

@Singleton
class UserPreferences @Inject constructor(@ApplicationContext context: Context){

    private val userData: DataStore<Preferences> = context.dataStore

    val loginPref = userData.data
        .catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else throw it
        }.map { preferences  ->
            val email = preferences[PreferenceKeys.USER_EMAIL]?: GUEST_EMAIL
            val password = preferences[PreferenceKeys.USER_PASSWORD]?: "password123"
            val uid = preferences[PreferenceKeys.USER_UID]?: "uid"
            LoginDetails(email, password, uid)
        }

    suspend fun updateLoginData(email: String, password: String, uid: String) {
        userData.edit { preferences ->
            preferences[PreferenceKeys.USER_EMAIL] = email
            preferences[PreferenceKeys.USER_PASSWORD] = password
            preferences[PreferenceKeys.USER_UID] = uid
        }
    }

    private object PreferenceKeys {
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_PASSWORD = stringPreferencesKey("user_password")
        val USER_UID = stringPreferencesKey("user_uid")
    }
}