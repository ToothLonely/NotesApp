package dev.toothlonely.notesapp.feature.settings.impl.presentation.settings.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.core.domain.model.AccentPreset
import dev.toothlonely.notesapp.core.domain.model.ThemeMode
import dev.toothlonely.notesapp.core.domain.model.UserPreferences
import dev.toothlonely.notesapp.feature.settings.impl.presentation.settings.SettingsPreferencesUiState
import dev.toothlonely.notesapp.feature.settings.impl.presentation.settings.components.AccentPaletteSelector
import dev.toothlonely.notesapp.feature.settings.impl.presentation.settings.components.ResetSettingsButton
import dev.toothlonely.notesapp.feature.settings.impl.presentation.settings.components.ThemeModeSelector

@Composable
fun SettingsContentScreen(
    state: SettingsPreferencesUiState.Content,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onAccentPresetSelected: (AccentPreset) -> Unit,
    onRequestReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val useDarkColors = when (state.userPreferences.themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    Column(modifier = modifier) {
        AccentPaletteSelector(
            selectedAccentPreset = state.userPreferences.accentPreset,
            useDarkColors = useDarkColors,
            isSaving = state.isAccentPresetSaving,
            enabled = !state.isAccentPresetSaving && !state.isResetting,
            onAccentPresetSelected = onAccentPresetSelected,
        )
        ThemeModeSelector(
            selectedThemeMode = state.userPreferences.themeMode,
            isSaving = state.isThemeModeSaving,
            enabled = !state.isThemeModeSaving && !state.isResetting,
            onThemeModeSelected = onThemeModeSelected,
            modifier = Modifier.padding(top = NotesAppSpacing.space6),
        )
        ResetSettingsButton(
            isResetting = state.isResetting,
            enabled = !state.isThemeModeSaving &&
                !state.isAccentPresetSaving &&
                !state.isResetting,
            onClick = onRequestReset,
            modifier = Modifier.padding(top = NotesAppSpacing.space8),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsContentScreenPreview() {
    NotesAppTheme {
        SettingsContentScreen(
            state = SettingsPreferencesUiState.Content(
                userPreferences = UserPreferences(),
            ),
            onThemeModeSelected = {},
            onAccentPresetSelected = {},
            onRequestReset = {},
            modifier = Modifier.padding(NotesAppSpacing.space4),
        )
    }
}
