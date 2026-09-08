package dev.toothlonely.notesapp.feature.settings.impl.testutil

import dev.toothlonely.notesapp.core.domain.model.AccentPreset
import dev.toothlonely.notesapp.core.domain.model.ThemeMode
import dev.toothlonely.notesapp.core.domain.model.UserPreferences
import dev.toothlonely.notesapp.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update

class FakeUserPreferencesRepository(
    initialPreferences: UserPreferences = UserPreferences(),
) : UserPreferencesRepository {
    val preferences = MutableStateFlow(initialPreferences)
    val selectedThemeModes = mutableListOf<ThemeMode>()
    val selectedAccentPresets = mutableListOf<AccentPreset>()
    var resetCount = 0

    var observeFailure: Throwable? = null
    var themeFailure: Throwable? = null
    var accentFailure: Throwable? = null
    var resetFailure: Throwable? = null
    var themeGate: CompletableDeferred<Unit>? = null
    var accentGate: CompletableDeferred<Unit>? = null
    var resetGate: CompletableDeferred<Unit>? = null

    override fun observeUserPreferences(): Flow<UserPreferences> = observeFailure?.let { failure ->
        flow { throw failure }
    } ?: preferences

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        val previousThemeMode = preferences.value.themeMode
        preferences.update { current -> current.copy(themeMode = themeMode) }
        try {
            themeGate?.await()
            themeFailure?.let { throw it }
            selectedThemeModes += themeMode
        } catch (error: Throwable) {
            preferences.update { current -> current.copy(themeMode = previousThemeMode) }
            throw error
        }
    }

    override suspend fun setAccentPreset(accentPreset: AccentPreset) {
        val previousAccentPreset = preferences.value.accentPreset
        preferences.update { current -> current.copy(accentPreset = accentPreset) }
        try {
            accentGate?.await()
            accentFailure?.let { throw it }
            selectedAccentPresets += accentPreset
        } catch (error: Throwable) {
            preferences.update { current -> current.copy(accentPreset = previousAccentPreset) }
            throw error
        }
    }

    override suspend fun resetAppearance() {
        val previousPreferences = preferences.value
        preferences.value = UserPreferences()
        try {
            resetGate?.await()
            resetFailure?.let { throw it }
            resetCount += 1
        } catch (error: Throwable) {
            preferences.value = previousPreferences
            throw error
        }
    }
}
