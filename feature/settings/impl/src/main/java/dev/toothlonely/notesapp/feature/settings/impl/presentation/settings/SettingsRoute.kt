package dev.toothlonely.notesapp.feature.settings.impl.presentation.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.toothlonely.notesapp.core.designsystem.component.AppSnackbarHost
import dev.toothlonely.notesapp.feature.settings.impl.R
import dev.toothlonely.notesapp.feature.settings.impl.presentation.settings.screens.SettingsScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsRoute(
    bottomNavigationPadding: PaddingValues,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val preferenceSaveFailedMessage = stringResource(R.string.settings_save_error)
    val resetSucceededMessage = stringResource(R.string.settings_reset_succeeded)
    val resetFailedMessage = stringResource(R.string.settings_reset_error)

    LaunchedEffect(
        viewModel,
        preferenceSaveFailedMessage,
        resetSucceededMessage,
        resetFailedMessage,
    ) {
        viewModel.events.collect { event ->
            val message = when (event) {
                SettingsEvent.PreferenceSaveFailed -> preferenceSaveFailedMessage
                SettingsEvent.ResetSucceeded -> resetSucceededMessage
                SettingsEvent.ResetFailed -> resetFailedMessage
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    SettingsScreen(
        state = state,
        bottomNavigationPadding = bottomNavigationPadding,
        onThemeModeSelected = viewModel::selectThemeMode,
        onAccentPresetSelected = viewModel::selectAccentPreset,
        onRetryPreferences = viewModel::retryPreferences,
        onRetryBalance = viewModel::retryBalance,
        onRequestReset = viewModel::requestReset,
        onCancelReset = viewModel::cancelReset,
        onConfirmReset = viewModel::confirmReset,
        snackbarHost = { AppSnackbarHost(hostState = snackbarHostState) },
    )
}
