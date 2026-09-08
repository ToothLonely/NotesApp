package dev.toothlonely.notesapp.feature.settings.impl.presentation.settings

sealed interface SettingsEvent {
    data object PreferenceSaveFailed : SettingsEvent

    data object ResetSucceeded : SettingsEvent

    data object ResetFailed : SettingsEvent
}
