package dev.toothlonely.notesapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppAccentPalette
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppThemeMode
import dev.toothlonely.notesapp.core.domain.model.AccentPreset
import dev.toothlonely.notesapp.core.domain.model.ThemeMode
import dev.toothlonely.notesapp.core.domain.model.UserPreferences
import dev.toothlonely.notesapp.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch

class AppThemeViewModel(
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(UserPreferences())
    val state: StateFlow<UserPreferences> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.observeUserPreferences()
                .retryWhen { error, _ ->
                    if (error is CancellationException) {
                        false
                    } else {
                        delay(PREFERENCES_RETRY_DELAY_MILLIS)
                        true
                    }
                }
                .collect { preferences -> _state.value = preferences }
        }
    }

    private companion object {
        const val PREFERENCES_RETRY_DELAY_MILLIS = 1_000L
    }
}

internal fun ThemeMode.toDesignSystemThemeMode(): NotesAppThemeMode = when (this) {
    ThemeMode.System -> NotesAppThemeMode.System
    ThemeMode.Light -> NotesAppThemeMode.Light
    ThemeMode.Dark -> NotesAppThemeMode.Dark
}

internal fun ThemeMode.shouldUseDarkTheme(systemInDarkTheme: Boolean): Boolean = when (this) {
    ThemeMode.System -> systemInDarkTheme
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
}

internal fun AccentPreset.toDesignSystemAccentPalette(): NotesAppAccentPalette = when (this) {
    AccentPreset.Indigo -> NotesAppAccentPalette.Indigo
    AccentPreset.Teal -> NotesAppAccentPalette.Teal
    AccentPreset.Raspberry -> NotesAppAccentPalette.Raspberry
    AccentPreset.Amber -> NotesAppAccentPalette.Amber
}
