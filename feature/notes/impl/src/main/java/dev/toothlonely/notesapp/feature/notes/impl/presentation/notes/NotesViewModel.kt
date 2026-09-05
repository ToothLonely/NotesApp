package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.toothlonely.notesapp.feature.notes.impl.domain.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class NotesViewModel(
    private val notesRepository: NotesRepository,
) : ViewModel() {
    private val retryCount = MutableStateFlow(0)

    val state: StateFlow<NotesUiState> = retryCount
        .flatMapLatest {
            flow { emitAll(notesRepository.observeNotes()) }
                .map { notes ->
                    if (notes.isEmpty()) NotesUiState.Empty else NotesUiState.Content(notes)
                }
                .onStart { emit(NotesUiState.Loading) }
                .catch { emit(NotesUiState.Error) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = NotesUiState.Loading,
        )

    fun retry() {
        retryCount.update(Int::inc)
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
