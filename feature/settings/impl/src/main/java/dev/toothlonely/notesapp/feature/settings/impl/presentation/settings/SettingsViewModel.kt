package dev.toothlonely.notesapp.feature.settings.impl.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.toothlonely.notesapp.core.domain.model.AccentPreset
import dev.toothlonely.notesapp.core.domain.model.ThemeMode
import dev.toothlonely.notesapp.core.domain.model.UserPreferences
import dev.toothlonely.notesapp.core.domain.repository.GigaChatRepository
import dev.toothlonely.notesapp.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val gigaChatRepository: GigaChatRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    private val eventChannel = Channel<SettingsEvent>(capacity = Channel.BUFFERED)
    val events: Flow<SettingsEvent> = eventChannel.receiveAsFlow()

    private var observationJob: Job? = null
    private var balanceJob: Job? = null

    init {
        observePreferences()
        loadBalance()
    }

    fun retryPreferences() {
        observePreferences()
    }

    fun retryBalance() {
        loadBalance()
    }

    fun selectThemeMode(themeMode: ThemeMode) {
        val content = currentContent() ?: return
        if (
            content.isThemeModeSaving ||
            content.isResetting ||
            content.userPreferences.themeMode == themeMode
        ) {
            return
        }
        val previousThemeMode = content.userPreferences.themeMode

        updateContent { current ->
            current.copy(
                userPreferences = current.userPreferences.copy(themeMode = themeMode),
                isThemeModeSaving = true,
            )
        }
        viewModelScope.launch {
            try {
                userPreferencesRepository.setThemeMode(themeMode)
                updateContent { current ->
                    current.copy(isThemeModeSaving = false)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                updateContent { current ->
                    current.copy(
                        userPreferences = current.userPreferences.copy(
                            themeMode = previousThemeMode,
                        ),
                        isThemeModeSaving = false,
                    )
                }
                eventChannel.send(SettingsEvent.PreferenceSaveFailed)
            }
        }
    }

    fun selectAccentPreset(accentPreset: AccentPreset) {
        val content = currentContent() ?: return
        if (
            content.isAccentPresetSaving ||
            content.isResetting ||
            content.userPreferences.accentPreset == accentPreset
        ) {
            return
        }
        val previousAccentPreset = content.userPreferences.accentPreset

        updateContent { current ->
            current.copy(
                userPreferences = current.userPreferences.copy(accentPreset = accentPreset),
                isAccentPresetSaving = true,
            )
        }
        viewModelScope.launch {
            try {
                userPreferencesRepository.setAccentPreset(accentPreset)
                updateContent { current ->
                    current.copy(isAccentPresetSaving = false)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                updateContent { current ->
                    current.copy(
                        userPreferences = current.userPreferences.copy(
                            accentPreset = previousAccentPreset,
                        ),
                        isAccentPresetSaving = false,
                    )
                }
                eventChannel.send(SettingsEvent.PreferenceSaveFailed)
            }
        }
    }

    fun requestReset() {
        updateContent { content ->
            if (
                content.isThemeModeSaving ||
                content.isAccentPresetSaving ||
                content.isResetting
            ) {
                content
            } else {
                content.copy(showResetConfirmation = true)
            }
        }
    }

    fun cancelReset() {
        updateContent { content ->
            if (content.isResetting) content else content.copy(showResetConfirmation = false)
        }
    }

    fun confirmReset() {
        val content = currentContent() ?: return
        if (!content.showResetConfirmation || content.isResetting) return

        val previousPreferences = content.userPreferences
        val defaults = UserPreferences()
        updateContent { current ->
            current.copy(
                userPreferences = defaults,
                isResetting = true,
                showResetConfirmation = false,
            )
        }
        viewModelScope.launch {
            try {
                userPreferencesRepository.resetAppearance()
                updateContent { current ->
                    current.copy(
                        userPreferences = defaults,
                        isResetting = false,
                    )
                }
                eventChannel.send(SettingsEvent.ResetSucceeded)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                updateContent { current ->
                    current.copy(
                        userPreferences = previousPreferences,
                        isResetting = false,
                    )
                }
                eventChannel.send(SettingsEvent.ResetFailed)
            }
        }
    }

    private fun observePreferences() {
        observationJob?.cancel()
        _state.update { state ->
            state.copy(preferencesState = SettingsPreferencesUiState.Loading)
        }
        observationJob = viewModelScope.launch {
            try {
                userPreferencesRepository.observeUserPreferences().collect { observedPreferences ->
                    _state.update { state ->
                        val current = state.preferencesState as? SettingsPreferencesUiState.Content
                        state.copy(
                            preferencesState = current?.copy(
                                userPreferences = observedPreferences,
                            ) ?: SettingsPreferencesUiState.Content(observedPreferences),
                        )
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                _state.update { state ->
                    state.copy(preferencesState = SettingsPreferencesUiState.Error)
                }
            }
        }
    }

    private fun loadBalance() {
        if (balanceJob?.isActive == true) return
        _state.update { state -> state.copy(balanceState = GigaChatBalanceUiState.Loading) }
        balanceJob = viewModelScope.launch {
            _state.update { state ->
                state.copy(
                    balanceState = try {
                        GigaChatBalanceUiState.Content(
                            tokenCount = gigaChatRepository.getBalance().totalTokens,
                        )
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Throwable) {
                        GigaChatBalanceUiState.Error
                    },
                )
            }
        }
    }

    private fun currentContent(): SettingsPreferencesUiState.Content? =
        _state.value.preferencesState as? SettingsPreferencesUiState.Content

    private fun updateContent(
        transform: (SettingsPreferencesUiState.Content) -> SettingsPreferencesUiState.Content,
    ) {
        _state.update { state ->
            val content = state.preferencesState as? SettingsPreferencesUiState.Content
                ?: return@update state
            state.copy(preferencesState = transform(content))
        }
    }
}
