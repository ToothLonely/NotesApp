package dev.toothlonely.notesapp.feature.settings.impl.presentation.settings

import dev.toothlonely.notesapp.core.domain.model.UserPreferences

data class SettingsUiState(
    val preferencesState: SettingsPreferencesUiState = SettingsPreferencesUiState.Loading,
    val balanceState: GigaChatBalanceUiState = GigaChatBalanceUiState.Unavailable,
)

sealed interface SettingsPreferencesUiState {
    data object Loading : SettingsPreferencesUiState

    data object Error : SettingsPreferencesUiState

    data class Content(
        val userPreferences: UserPreferences,
        val isThemeModeSaving: Boolean = false,
        val isAccentPresetSaving: Boolean = false,
        val isResetting: Boolean = false,
        val showResetConfirmation: Boolean = false,
    ) : SettingsPreferencesUiState
}

sealed interface GigaChatBalanceUiState {
    data object Unavailable : GigaChatBalanceUiState
}
