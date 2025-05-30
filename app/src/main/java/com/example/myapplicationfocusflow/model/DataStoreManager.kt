package com.example.myapplicationfocusflow.model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "focusflow_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        private val FOCUS_MODELS_KEY = stringPreferencesKey("focus_models")
        private val CATEGORIES_KEY = stringPreferencesKey("categories")
        private val AMBIENT_SOUNDS_KEY = stringPreferencesKey("ambient_sounds")
        private val FOCUS_SESSIONS_KEY = stringPreferencesKey("focus_sessions")
        private val NEXT_FOCUS_ID_KEY = stringPreferencesKey("next_focus_id")
        private val NEXT_SESSION_ID_KEY = stringPreferencesKey("next_session_id")
    }

    // Focus Models
    suspend fun saveFocusModels(focusModels: List<FocusModel>) {
        context.dataStore.edit { preferences ->
            preferences[FOCUS_MODELS_KEY] = Json.encodeToString(focusModels)
        }
    }

    fun getFocusModels(): Flow<List<FocusModel>> {
        return context.dataStore.data.map { preferences ->
            val json = preferences[FOCUS_MODELS_KEY] ?: "[]"
            try {
                Json.decodeFromString<List<FocusModel>>(json)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    // Categories
    suspend fun saveCategories(categories: List<CategoryModel>) {
        context.dataStore.edit { preferences ->
            preferences[CATEGORIES_KEY] = Json.encodeToString(categories)
        }
    }

    fun getCategories(): Flow<List<CategoryModel>> {
        return context.dataStore.data.map { preferences ->
            val json = preferences[CATEGORIES_KEY] ?: "[]"
            try {
                Json.decodeFromString<List<CategoryModel>>(json)
            } catch (e: Exception) {
                getDefaultCategories()
            }
        }
    }

    // Ambient Sounds
    suspend fun saveAmbientSounds(sounds: List<AmbientSoundModel>) {
        context.dataStore.edit { preferences ->
            preferences[AMBIENT_SOUNDS_KEY] = Json.encodeToString(sounds)
        }
    }

    fun getAmbientSounds(): Flow<List<AmbientSoundModel>> {
        return context.dataStore.data.map { preferences ->
            val json = preferences[AMBIENT_SOUNDS_KEY] ?: "[]"
            try {
                Json.decodeFromString<List<AmbientSoundModel>>(json)
            } catch (e: Exception) {
                getDefaultAmbientSounds()
            }
        }
    }

    // Focus Sessions
    suspend fun saveFocusSessions(sessions: List<FocusSessionModel>) {
        context.dataStore.edit { preferences ->
            preferences[FOCUS_SESSIONS_KEY] = Json.encodeToString(sessions)
        }
    }

    fun getFocusSessions(): Flow<List<FocusSessionModel>> {
        return context.dataStore.data.map { preferences ->
            val json = preferences[FOCUS_SESSIONS_KEY] ?: "[]"
            try {
                Json.decodeFromString<List<FocusSessionModel>>(json)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    // Next IDs
    suspend fun getNextFocusId(): Int {
        return context.dataStore.data.first()[NEXT_FOCUS_ID_KEY]?.toIntOrNull() ?: 1
    }

    suspend fun incrementFocusId() {
        try {
            context.dataStore.edit { preferences ->
                val currentId = preferences[NEXT_FOCUS_ID_KEY]?.toIntOrNull() ?: 1
                preferences[NEXT_FOCUS_ID_KEY] = (currentId + 1).toString()
            }
        } catch (e: Exception) {
            // Log error or handle appropriately
        }
    }

    suspend fun getNextSessionId(): Int {
        return context.dataStore.data.first()[NEXT_SESSION_ID_KEY]?.toIntOrNull() ?: 1
    }

    suspend fun validateDataIntegrity(): Boolean {
        return try {
            getFocusModels().first()
            getCategories().first()
            getAmbientSounds().first()
            getFocusSessions().first()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun incrementSessionId() {
        context.dataStore.edit { preferences ->
            val currentId = preferences[NEXT_SESSION_ID_KEY]?.toIntOrNull() ?: 1
            preferences[NEXT_SESSION_ID_KEY] = (currentId + 1).toString()
        }
    }

    private fun getDefaultCategories(): List<CategoryModel> {
        return listOf(
            CategoryModel(1, "Study"),
            CategoryModel(2, "Work"),
            CategoryModel(3, "Reading"),
            CategoryModel(4, "Exercise"),
            CategoryModel(5, "Meditation")
        )
    }

    private fun getDefaultAmbientSounds(): List<AmbientSoundModel> {
        return listOf(
            AmbientSoundModel(1, "Rain", "rain.mp3"),
            AmbientSoundModel(2, "Forest", "forest.mp3"),
            AmbientSoundModel(3, "Ocean", "ocean.mp3"),
            AmbientSoundModel(4, "White Noise", "whitenoise.mp3"),
            AmbientSoundModel(5, "No Sound", "")
        )
    }
}
