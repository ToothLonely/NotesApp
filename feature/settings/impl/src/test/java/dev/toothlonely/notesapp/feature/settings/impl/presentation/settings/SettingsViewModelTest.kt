package dev.toothlonely.notesapp.feature.settings.impl.presentation.settings

import dev.toothlonely.notesapp.core.domain.model.AccentPreset
import dev.toothlonely.notesapp.core.domain.model.ThemeMode
import dev.toothlonely.notesapp.core.domain.model.UserPreferences
import dev.toothlonely.notesapp.feature.settings.impl.testutil.FakeUserPreferencesRepository
import dev.toothlonely.notesapp.feature.settings.impl.testutil.FakeGigaChatRepository
import dev.toothlonely.notesapp.feature.settings.impl.testutil.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `preferences resolve while balance is still loading`() = runTest {
        val balanceGate = CompletableDeferred<Unit>()
        val gigaChatRepository = FakeGigaChatRepository().apply {
            this.balanceGate = balanceGate
        }
        val viewModel = createViewModel(
            preferencesRepository = FakeUserPreferencesRepository(),
            gigaChatRepository = gigaChatRepository,
        )

        assertEquals(SettingsPreferencesUiState.Loading, viewModel.state.value.preferencesState)
        assertEquals(GigaChatBalanceUiState.Loading, viewModel.state.value.balanceState)

        runCurrent()

        assertEquals(
            SettingsPreferencesUiState.Content(UserPreferences()),
            viewModel.state.value.preferencesState,
        )
        assertEquals(GigaChatBalanceUiState.Loading, viewModel.state.value.balanceState)

        balanceGate.complete(Unit)
        runCurrent()
        assertEquals(
            GigaChatBalanceUiState.Content(tokenCount = 12_480),
            viewModel.state.value.balanceState,
        )
    }

    @Test
    fun `balance failure is retryable without blocking preferences`() = runTest {
        val gigaChatRepository = FakeGigaChatRepository().apply {
            balanceFailure = IllegalStateException("Network unavailable")
        }
        val viewModel = createViewModel(
            preferencesRepository = FakeUserPreferencesRepository(),
            gigaChatRepository = gigaChatRepository,
        )
        runCurrent()

        assertEquals(GigaChatBalanceUiState.Error, viewModel.state.value.balanceState)
        assertTrue(viewModel.state.value.preferencesState is SettingsPreferencesUiState.Content)

        gigaChatRepository.balanceFailure = null
        viewModel.retryBalance()
        assertEquals(GigaChatBalanceUiState.Loading, viewModel.state.value.balanceState)
        runCurrent()

        assertEquals(
            GigaChatBalanceUiState.Content(tokenCount = 12_480),
            viewModel.state.value.balanceState,
        )
        assertEquals(2, gigaChatRepository.balanceRequestCount)
    }

    @Test
    fun `preference read failure shows error and retry subscribes again`() = runTest {
        val repository = FakeUserPreferencesRepository().apply {
            observeFailure = IllegalStateException("Preferences unavailable")
        }
        val viewModel = createViewModel(repository)

        runCurrent()
        assertEquals(SettingsPreferencesUiState.Error, viewModel.state.value.preferencesState)

        repository.observeFailure = null
        viewModel.retryPreferences()
        assertEquals(SettingsPreferencesUiState.Loading, viewModel.state.value.preferencesState)
        runCurrent()

        assertEquals(
            SettingsPreferencesUiState.Content(UserPreferences()),
            viewModel.state.value.preferencesState,
        )
    }

    @Test
    fun `theme selection is optimistic and persists only once`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repository = FakeUserPreferencesRepository().apply { themeGate = gate }
        val viewModel = createViewModel(repository)
        runCurrent()

        viewModel.selectThemeMode(ThemeMode.Dark)
        viewModel.selectThemeMode(ThemeMode.Light)
        runCurrent()

        val saving = viewModel.contentState()
        assertEquals(ThemeMode.Dark, saving.userPreferences.themeMode)
        assertTrue(saving.isThemeModeSaving)
        assertTrue(repository.selectedThemeModes.isEmpty())

        gate.complete(Unit)
        runCurrent()

        val saved = viewModel.contentState()
        assertEquals(ThemeMode.Dark, saved.userPreferences.themeMode)
        assertFalse(saved.isThemeModeSaving)
        assertEquals(listOf(ThemeMode.Dark), repository.selectedThemeModes)
    }

    @Test
    fun `theme write failure rolls back selection and reports failure`() = runTest {
        val repository = FakeUserPreferencesRepository(
            UserPreferences(themeMode = ThemeMode.Light),
        ).apply {
            themeFailure = IllegalStateException("Write failed")
        }
        val viewModel = createViewModel(repository)
        runCurrent()
        val event = async { viewModel.events.first() }

        viewModel.selectThemeMode(ThemeMode.Dark)
        runCurrent()

        val content = viewModel.contentState()
        assertEquals(ThemeMode.Light, content.userPreferences.themeMode)
        assertFalse(content.isThemeModeSaving)
        assertEquals(SettingsEvent.PreferenceSaveFailed, event.await())
    }

    @Test
    fun `accent selection persists while theme control remains independent`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repository = FakeUserPreferencesRepository().apply { accentGate = gate }
        val viewModel = createViewModel(repository)
        runCurrent()

        viewModel.selectAccentPreset(AccentPreset.Raspberry)
        runCurrent()

        val saving = viewModel.contentState()
        assertEquals(AccentPreset.Raspberry, saving.userPreferences.accentPreset)
        assertTrue(saving.isAccentPresetSaving)
        assertFalse(saving.isThemeModeSaving)

        gate.complete(Unit)
        runCurrent()

        assertEquals(listOf(AccentPreset.Raspberry), repository.selectedAccentPresets)
        assertFalse(viewModel.contentState().isAccentPresetSaving)
    }

    @Test
    fun `reset requires confirmation and cancel leaves preferences unchanged`() = runTest {
        val original = UserPreferences(ThemeMode.Dark, AccentPreset.Amber)
        val repository = FakeUserPreferencesRepository(original)
        val viewModel = createViewModel(repository)
        runCurrent()

        viewModel.requestReset()
        assertTrue(viewModel.contentState().showResetConfirmation)
        assertEquals(0, repository.resetCount)

        viewModel.cancelReset()

        assertFalse(viewModel.contentState().showResetConfirmation)
        assertEquals(original, viewModel.contentState().userPreferences)
        assertEquals(0, repository.resetCount)
    }

    @Test
    fun `confirmed reset applies defaults optimistically and reports success`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repository = FakeUserPreferencesRepository(
            UserPreferences(ThemeMode.Dark, AccentPreset.Amber),
        ).apply { resetGate = gate }
        val viewModel = createViewModel(repository)
        runCurrent()
        val event = async { viewModel.events.first() }

        viewModel.requestReset()
        viewModel.confirmReset()
        runCurrent()

        val resetting = viewModel.contentState()
        assertEquals(UserPreferences(), resetting.userPreferences)
        assertTrue(resetting.isResetting)
        assertFalse(resetting.showResetConfirmation)

        gate.complete(Unit)
        runCurrent()

        assertEquals(1, repository.resetCount)
        assertFalse(viewModel.contentState().isResetting)
        assertEquals(SettingsEvent.ResetSucceeded, event.await())
    }

    @Test
    fun `reset failure restores saved preferences and reports failure`() = runTest {
        val original = UserPreferences(ThemeMode.Light, AccentPreset.Teal)
        val repository = FakeUserPreferencesRepository(original).apply {
            resetFailure = IllegalStateException("Reset failed")
        }
        val viewModel = createViewModel(repository)
        runCurrent()
        val event = async { viewModel.events.first() }

        viewModel.requestReset()
        viewModel.confirmReset()
        runCurrent()

        val content = viewModel.contentState()
        assertEquals(original, content.userPreferences)
        assertFalse(content.isResetting)
        assertEquals(SettingsEvent.ResetFailed, event.await())
    }

    private fun SettingsViewModel.contentState(): SettingsPreferencesUiState.Content =
        state.value.preferencesState as SettingsPreferencesUiState.Content

    private fun createViewModel(
        preferencesRepository: FakeUserPreferencesRepository,
        gigaChatRepository: FakeGigaChatRepository = FakeGigaChatRepository(),
    ) = SettingsViewModel(
        userPreferencesRepository = preferencesRepository,
        gigaChatRepository = gigaChatRepository,
    )
}
