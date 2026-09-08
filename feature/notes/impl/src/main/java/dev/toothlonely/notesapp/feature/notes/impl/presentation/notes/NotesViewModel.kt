package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.Note
import dev.toothlonely.notesapp.feature.notes.impl.domain.usecase.NoteListProcessor
import dev.toothlonely.notesapp.feature.notes.impl.domain.repository.NotesRepository
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NotesSortOrder
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NotesViewMode
import dev.toothlonely.notesapp.feature.notes.impl.domain.repository.NotesViewModeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class NotesViewModel(
    private val notesRepository: NotesRepository,
    private val notesViewModeRepository: NotesViewModeRepository,
    private val noteListProcessor: NoteListProcessor,
    searchDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private val retryCount = MutableStateFlow(0)
    private val controlsState = MutableStateFlow(NotesControlsState())
    private val eventChannel = Channel<NotesEvent>(capacity = Channel.BUFFERED)
    private val lastObservedNotesCount = AtomicInteger(UNINITIALIZED_NOTES_COUNT)
    private val notesRevision = AtomicLong(0L)

    val events: Flow<NotesEvent> = eventChannel.receiveAsFlow()

    val state: StateFlow<NotesUiState> = combine(
        observeContentState(searchDispatcher),
        controlsState,
        notesViewModeRepository.observeViewMode().catch {
            emit(NotesViewMode.List)
        },
    ) { contentSnapshot, controls, viewMode ->
        NotesUiState(
            content = contentSnapshot.content,
            notesRevision = contentSnapshot.notesRevision,
            scrollToStartOnNotesRevision = contentSnapshot.scrollToStart,
            draftQuery = controls.draftQuery,
            appliedQuery = controls.appliedQuery,
            sortOrder = controls.sortOrder,
            viewMode = viewMode,
            isViewModeSaving = controls.isViewModeSaving,
            hasViewModeSaveError = controls.failedViewMode != null,
            isDeleteMode = controls.isDeleteMode,
            deleteConfirmation = controls.deleteConfirmation,
            deletingNoteIds = controls.deletingNoteIds,
            failedDeleteNoteId = controls.failedDeleteNoteId,
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

    fun updateDraftQuery(query: String) {
        controlsState.update { state -> state.copy(draftQuery = query) }
    }

    fun applySearch() {
        controlsState.update { state ->
            state.copy(appliedQuery = state.draftQuery.trim())
        }
    }

    fun clearSearch() {
        controlsState.update { state ->
            state.copy(
                draftQuery = "",
                appliedQuery = "",
            )
        }
    }

    fun changeSortOrder(sortOrder: NotesSortOrder) {
        controlsState.update { state -> state.copy(sortOrder = sortOrder) }
    }

    fun changeViewMode(viewMode: NotesViewMode) {
        val currentState = state.value
        if (currentState.viewMode == viewMode || currentState.isViewModeSaving) return

        controlsState.update { controls ->
            controls.copy(
                isViewModeSaving = true,
                failedViewMode = null,
            )
        }
        viewModelScope.launch {
            try {
                notesViewModeRepository.setViewMode(viewMode)
                controlsState.update { controls -> controls.copy(isViewModeSaving = false) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                controlsState.update { controls ->
                    controls.copy(
                        isViewModeSaving = false,
                        failedViewMode = viewMode,
                    )
                }
            }
        }
    }

    fun retryViewModeChange() {
        controlsState.value.failedViewMode?.let(::changeViewMode)
    }

    fun dismissViewModeSaveError() {
        controlsState.update { state -> state.copy(failedViewMode = null) }
    }

    fun toggleDeleteMode() {
        controlsState.update { state ->
            state.copy(
                isDeleteMode = !state.isDeleteMode,
                deleteConfirmation = null,
                failedDeleteNoteId = null,
            )
        }
    }

    fun exitDeleteMode() {
        controlsState.update { state ->
            state.copy(
                isDeleteMode = false,
                deleteConfirmation = null,
                failedDeleteNoteId = null,
            )
        }
    }

    fun requestDeleteNote(noteId: Long, noteTitle: String) {
        val currentState = controlsState.value
        if (
            !currentState.isDeleteMode ||
            currentState.deleteConfirmation != null ||
            noteId in currentState.deletingNoteIds
        ) {
            return
        }

        controlsState.update { state ->
            state.copy(
                deleteConfirmation = DeleteNoteConfirmationUiState(
                    noteId = noteId,
                    noteTitle = noteTitle,
                ),
                failedDeleteNoteId = null,
            )
        }
    }

    fun cancelDeleteNote() {
        controlsState.update { state ->
            if (state.deleteConfirmation?.isDeleting == true) state
            else state.copy(deleteConfirmation = null)
        }
    }

    fun confirmDeleteNote() {
        val confirmation = controlsState.value.deleteConfirmation ?: return
        if (confirmation.isDeleting) return

        controlsState.update { state ->
            state.copy(
                deleteConfirmation = state.deleteConfirmation?.copy(isDeleting = true),
                failedDeleteNoteId = null,
            )
        }
        deleteNote(confirmation.noteId)
    }

    private fun deleteNote(noteId: Long) {
        val currentState = controlsState.value
        if (!currentState.isDeleteMode || noteId in currentState.deletingNoteIds) return

        controlsState.update { state ->
            state.copy(
                deletingNoteIds = state.deletingNoteIds + noteId,
                failedDeleteNoteId = null,
            )
        }
        viewModelScope.launch {
            try {
                check(notesRepository.deleteNote(noteId))
                controlsState.update { state ->
                    state.copy(
                        deleteConfirmation = null,
                        deletingNoteIds = state.deletingNoteIds - noteId,
                    )
                }
                eventChannel.send(NotesEvent.NoteDeleted)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                controlsState.update { state ->
                    state.copy(
                        deleteConfirmation = null,
                        deletingNoteIds = state.deletingNoteIds - noteId,
                        failedDeleteNoteId = noteId.takeIf { state.isDeleteMode },
                    )
                }
            }
        }
    }

    fun retryDelete() {
        controlsState.value.failedDeleteNoteId?.let(::deleteNote)
    }

    fun dismissDeleteError() {
        controlsState.update { state -> state.copy(failedDeleteNoteId = null) }
    }

    private fun observeNotesLoadState(): Flow<NotesLoadState> = retryCount
        .flatMapLatest {
            flow { emitAll(notesRepository.observeNotes()) }
                .map<List<Note>, NotesLoadState> { notes ->
                    val previousNotesCount = lastObservedNotesCount.getAndSet(notes.size)
                    NotesLoadState.Loaded(
                        notes = notes,
                        notesRevision = notesRevision.incrementAndGet(),
                        scrollToStart = previousNotesCount != UNINITIALIZED_NOTES_COUNT &&
                            notes.size >= previousNotesCount,
                    )
                }
                .onStart { emit(NotesLoadState.Loading) }
                .catch { emit(NotesLoadState.Error) }
        }

    private fun observeContentState(
        searchDispatcher: CoroutineDispatcher,
    ): Flow<NotesContentSnapshot> = combine(
        observeNotesLoadState(),
        controlsState
            .map { controls ->
                SearchCriteria(
                    appliedQuery = controls.appliedQuery,
                    sortOrder = controls.sortOrder,
                )
            }
            .distinctUntilChanged(),
    ) { notesLoadState, searchCriteria -> notesLoadState to searchCriteria }
        .mapLatest { (notesLoadState, searchCriteria) ->
            NotesContentSnapshot(
                content = notesLoadState.toContentState(searchCriteria),
                notesRevision = notesLoadState.notesRevision,
                scrollToStart = notesLoadState.scrollToStart,
            )
        }
        .flowOn(searchDispatcher)

    private fun NotesLoadState.toContentState(criteria: SearchCriteria): NotesContentState =
        when (this) {
            NotesLoadState.Loading -> NotesContentState.Loading
            NotesLoadState.Error -> NotesContentState.Error
            is NotesLoadState.Loaded -> {
                if (notes.isEmpty()) {
                    NotesContentState.Empty
                } else {
                    val visibleNotes = noteListProcessor.process(
                        notes = notes,
                        appliedQuery = criteria.appliedQuery,
                        sortOrder = criteria.sortOrder,
                    )
                    if (visibleNotes.isEmpty()) {
                        NotesContentState.SearchEmpty
                    } else {
                        NotesContentState.Content(visibleNotes)
                    }
                }
            }
        }

    private companion object {
        const val UNINITIALIZED_NOTES_COUNT = -1
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
