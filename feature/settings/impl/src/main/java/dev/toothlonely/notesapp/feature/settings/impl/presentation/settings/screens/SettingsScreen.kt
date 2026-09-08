package dev.toothlonely.notesapp.feature.settings.impl.presentation.settings.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.core.designsystem.component.BottomNavigationAwareSnackbarHost
import dev.toothlonely.notesapp.core.domain.model.AccentPreset
import dev.toothlonely.notesapp.core.domain.model.ThemeMode
import dev.toothlonely.notesapp.core.domain.model.UserPreferences
import dev.toothlonely.notesapp.feature.settings.impl.R
import dev.toothlonely.notesapp.feature.settings.impl.presentation.settings.SettingsPreferencesUiState
import dev.toothlonely.notesapp.feature.settings.impl.presentation.settings.SettingsUiState
import dev.toothlonely.notesapp.feature.settings.impl.presentation.settings.components.GigaChatBalanceCard
import dev.toothlonely.notesapp.feature.settings.impl.presentation.settings.components.ResetSettingsDialog
import dev.toothlonely.notesapp.feature.settings.impl.presentation.settings.components.SettingsTopBar

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    bottomNavigationPadding: PaddingValues = PaddingValues(0.dp),
    onThemeModeSelected: (ThemeMode) -> Unit,
    onAccentPresetSelected: (AccentPreset) -> Unit,
    onRetryPreferences: () -> Unit,
    onRetryBalance: () -> Unit,
    onRequestReset: () -> Unit,
    onCancelReset: () -> Unit,
    onConfirmReset: () -> Unit,
    snackbarHost: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenTitle = stringResource(R.string.settings_title)
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .semantics { paneTitle = screenTitle },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
        topBar = { SettingsTopBar(title = screenTitle) },
        snackbarHost = {
            BottomNavigationAwareSnackbarHost(
                bottomNavigationPadding = bottomNavigationPadding,
                isFloatingActionButtonVisible = false,
                snackbarHost = snackbarHost,
            )
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = NotesAppSizes.maximumContentWidth)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = NotesAppSpacing.space4)
                    .padding(top = NotesAppSpacing.space2)
                    .padding(bottomNavigationPadding)
                    .padding(bottom = NotesAppSpacing.space6),
            ) {
                GigaChatBalanceCard(
                    state = state.balanceState,
                    onRetry = onRetryBalance,
                )
                when (val preferencesState = state.preferencesState) {
                    SettingsPreferencesUiState.Loading -> SettingsPreferencesLoadingScreen(
                        modifier = Modifier.padding(top = NotesAppSpacing.space6),
                    )
                    SettingsPreferencesUiState.Error -> SettingsPreferencesErrorScreen(
                        onRetry = onRetryPreferences,
                        modifier = Modifier.padding(top = NotesAppSpacing.space6),
                    )
                    is SettingsPreferencesUiState.Content -> SettingsContentScreen(
                        state = preferencesState,
                        onThemeModeSelected = onThemeModeSelected,
                        onAccentPresetSelected = onAccentPresetSelected,
                        onRequestReset = onRequestReset,
                        modifier = Modifier.padding(top = NotesAppSpacing.space6),
                    )
                }
            }
        }
    }

    val content = state.preferencesState as? SettingsPreferencesUiState.Content
    if (content?.showResetConfirmation == true) {
        ResetSettingsDialog(
            onConfirm = onConfirmReset,
            onDismiss = onCancelReset,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    NotesAppTheme {
        SettingsScreen(
            state = SettingsUiState(
                preferencesState = SettingsPreferencesUiState.Content(
                    UserPreferences(
                        themeMode = ThemeMode.System,
                        accentPreset = AccentPreset.Indigo,
                    ),
                ),
            ),
            onThemeModeSelected = {},
            onAccentPresetSelected = {},
            onRetryPreferences = {},
            onRetryBalance = {},
            onRequestReset = {},
            onCancelReset = {},
            onConfirmReset = {},
            snackbarHost = {},
        )
    }
}
