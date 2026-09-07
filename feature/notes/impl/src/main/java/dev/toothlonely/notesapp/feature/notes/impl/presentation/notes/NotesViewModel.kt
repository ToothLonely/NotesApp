package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.toothlonely.notesapp.feature.notes.impl.domain.Note
import dev.toothlonely.notesapp.feature.notes.impl.domain.NotesRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class NotesViewModel(
    private val notesRepository: NotesRepository,
) : ViewModel() {
    private val retryCount = MutableStateFlow(0)
    private val deletionState = MutableStateFlow(DeletionState())
    private val eventChannel = Channel<NotesEvent>(capacity = Channel.BUFFERED)

    val events: Flow<NotesEvent> = eventChannel.receiveAsFlow()

    val state: StateFlow<NotesUiState> = combine(
        observeContentState(),
        deletionState,
    ) { content, deletion ->
        NotesUiState(
            content = content,
            isDeleteMode = deletion.isDeleteMode,
            deletingNoteIds = deletion.deletingNoteIds,
            failedDeleteNoteId = deletion.failedDeleteNoteId,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = NotesUiState(),
    )

    init {
        viewModelScope.launch {
            runCatching { notesRepository.cleanupOrphanedImages() }
        }
    }

    fun retryLoading() {
        retryCount.update(Int::inc)
    }

    fun toggleDeleteMode() {
        deletionState.update { state ->
            state.copy(
                isDeleteMode = !state.isDeleteMode,
                failedDeleteNoteId = null,
            )
        }
    }

    fun exitDeleteMode() {
        deletionState.update { state ->
            state.copy(
                isDeleteMode = false,
                failedDeleteNoteId = null,
            )
        }
    }

    fun deleteNote(noteId: Long) {
        val currentState = deletionState.value
        if (!currentState.isDeleteMode || noteId in currentState.deletingNoteIds) return

        deletionState.update { state ->
            state.copy(
                deletingNoteIds = state.deletingNoteIds + noteId,
                failedDeleteNoteId = null,
            )
        }
        viewModelScope.launch {
            runCatching { check(notesRepository.deleteNote(noteId)) }
                .onSuccess {
                    deletionState.update { state ->
                        state.copy(deletingNoteIds = state.deletingNoteIds - noteId)
                    }
                    eventChannel.send(NotesEvent.NoteDeleted)
                }
                .onFailure {
                    deletionState.update { state ->
                        state.copy(
                            deletingNoteIds = state.deletingNoteIds - noteId,
                            failedDeleteNoteId = noteId.takeIf { state.isDeleteMode },
                        )
                    }
                }
        }
    }

    fun retryDelete() {
        deletionState.value.failedDeleteNoteId?.let(::deleteNote)
    }

    fun dismissDeleteError() {
        deletionState.update { state -> state.copy(failedDeleteNoteId = null) }
    }

    private fun observeContentState(): Flow<NotesContentState> = retryCount
        .flatMapLatest {
            flow { emitAll(notesRepository.observeNotes()) }
                .map<List<Note>, NotesContentState> { notes ->
                    if (notes.isEmpty()) {
                        NotesContentState.Empty
                    } else {
                        NotesContentState.Content(notes)
                    }
                }
                .onStart { emit(NotesContentState.Loading) }
                .catch { emit(NotesContentState.Error) }
        }

    private data class DeletionState(
        val isDeleteMode: Boolean = false,
        val deletingNoteIds: Set<Long> = emptySet(),
        val failedDeleteNoteId: Long? = null,
    )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
