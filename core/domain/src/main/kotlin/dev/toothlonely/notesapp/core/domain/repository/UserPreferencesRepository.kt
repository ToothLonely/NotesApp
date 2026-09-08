package dev.toothlonely.notesapp.core.domain.repository

import dev.toothlonely.notesapp.core.domain.model.AccentPreset
import dev.toothlonely.notesapp.core.domain.model.ThemeMode
import dev.toothlonely.notesapp.core.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    /**
     * Emits the current appearance, including a pending user choice while it is being persisted.
     * A failed write restores the last durable value before the write method rethrows the error.
     */
    fun observeUserPreferences(): Flow<UserPreferences>

    suspend fun setThemeMode(themeMode: ThemeMode)

    suspend fun setAccentPreset(accentPreset: AccentPreset)

    suspend fun resetAppearance()
}
