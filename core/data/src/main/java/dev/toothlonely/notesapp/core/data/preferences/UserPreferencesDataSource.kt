package dev.toothlonely.notesapp.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.toothlonely.notesapp.core.domain.model.AccentPreset
import dev.toothlonely.notesapp.core.domain.model.ThemeMode
import dev.toothlonely.notesapp.core.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class UserPreferencesDataSource(
    private val dataStore: DataStore<Preferences>,
) {
    val userPreferences: Flow<UserPreferences> = dataStore.data
        .map { preferences -> preferences.toUserPreferences() }
        .distinctUntilChanged()

    suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.storageValue
        }
    }

    suspend fun setAccentPreset(accentPreset: AccentPreset) {
        dataStore.edit { preferences ->
            preferences[ACCENT_PRESET_KEY] = accentPreset.storageValue
        }
    }

    suspend fun resetAppearance() {
        dataStore.edit { preferences ->
            preferences.remove(THEME_MODE_KEY)
            preferences.remove(ACCENT_PRESET_KEY)
        }
    }

    private fun Preferences.toUserPreferences() = UserPreferences(
        themeMode = this[THEME_MODE_KEY].toThemeMode(),
        accentPreset = this[ACCENT_PRESET_KEY].toAccentPreset(),
    )

    private fun String?.toThemeMode(): ThemeMode = when (this) {
        THEME_MODE_LIGHT -> ThemeMode.Light
        THEME_MODE_DARK -> ThemeMode.Dark
        else -> ThemeMode.System
    }

    private fun String?.toAccentPreset(): AccentPreset = when (this) {
        ACCENT_PRESET_TEAL -> AccentPreset.Teal
        ACCENT_PRESET_RASPBERRY -> AccentPreset.Raspberry
        ACCENT_PRESET_AMBER -> AccentPreset.Amber
        else -> AccentPreset.Indigo
    }

    private val ThemeMode.storageValue: String
        get() = when (this) {
            ThemeMode.System -> THEME_MODE_SYSTEM
            ThemeMode.Light -> THEME_MODE_LIGHT
            ThemeMode.Dark -> THEME_MODE_DARK
        }

    private val AccentPreset.storageValue: String
        get() = when (this) {
            AccentPreset.Indigo -> ACCENT_PRESET_INDIGO
            AccentPreset.Teal -> ACCENT_PRESET_TEAL
            AccentPreset.Raspberry -> ACCENT_PRESET_RASPBERRY
            AccentPreset.Amber -> ACCENT_PRESET_AMBER
        }

    internal companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val ACCENT_PRESET_KEY = stringPreferencesKey("accent_preset")

        const val THEME_MODE_SYSTEM = "system"
        const val THEME_MODE_LIGHT = "light"
        const val THEME_MODE_DARK = "dark"
        const val ACCENT_PRESET_INDIGO = "indigo"
        const val ACCENT_PRESET_TEAL = "teal"
        const val ACCENT_PRESET_RASPBERRY = "raspberry"
        const val ACCENT_PRESET_AMBER = "amber"
    }
}
