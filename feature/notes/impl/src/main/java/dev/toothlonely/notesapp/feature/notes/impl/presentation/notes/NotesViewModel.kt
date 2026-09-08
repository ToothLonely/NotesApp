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
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

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
    private val notesRevision = AtomicLong(0L)
    private val lastObservation = AtomicReference<Pair<SearchCriteria, List<Note>>?>(null)

    val events: Flow<NotesEvent> = eventChannel.receiveAsFlow()

    val state: StateFlow<NotesUiState> = combine(
        observeContentState(searchDispatcher),
        controlsState,
        notesViewModeRepository.observeViewMode().catch {
            emit(NotesViewMode.List)
        },
    ) { contentSnapshot, controls, viewMode ->
        NotesUiState(
            content = when {
                controls.isSearchPending -> NotesContentState.SearchPending
                contentSnapshot.criteria.appliedQuery != controls.appliedQuery ||
                    contentSnapshot.criteria.sortOrder != controls.sortOrder -> NotesContentState.Loading
                else -> contentSnapshot.content
            },
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
        controlsState.update { state ->
            if (query == state.draftQuery) state else state.copy(
                draftQuery = query,
                appliedQuery = if (query.isEmpty()) "" else state.appliedQuery,
                isSearchPending = query.isNotEmpty(),
                visibleLimit = NOTES_PAGE_SIZE,
            )
        }
    }

    fun applySearch() {
        controlsState.update { state ->
            state.copy(
                appliedQuery = state.draftQuery.trim(),
                isSearchPending = state.draftQuery.isNotEmpty() && state.draftQuery.isBlank(),
                visibleLimit = NOTES_PAGE_SIZE,
            )
        }
    }

    fun clearSearch() {
        controlsState.update { state ->
            state.copy(
                draftQuery = "",
                appliedQuery = "",
                isSearchPending = false,
                visibleLimit = NOTES_PAGE_SIZE,
            )
        }
    }

    fun changeSortOrder(sortOrder: NotesSortOrder) {
        controlsState.update { state -> state.copy(sortOrder = sortOrder, visibleLimit = NOTES_PAGE_SIZE) }
    }

    fun loadMore() {
        val content = state.value.content as? NotesContentState.Content ?: return
        controlsState.update { controls ->
            if (!content.hasMore || controls.isSearchPending ||
                controls.visibleLimit != content.visibleLimit
            ) controls else controls.copy(visibleLimit = controls.visibleLimit + NOTES_PAGE_SIZE)
        }
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

    private fun observeContentState(
        searchDispatcher: CoroutineDispatcher,
    ): Flow<NotesContentSnapshot> = combine(
        retryCount,
        controlsState
            .map { controls ->
                SearchCriteria(
                    appliedQuery = controls.appliedQuery,
                    sortOrder = controls.sortOrder,
                    visibleLimit = controls.visibleLimit,
                    isSearchPending = controls.isSearchPending,
                )
            }
            .distinctUntilChanged(),
    ) { _, criteria -> criteria }
        .flatMapLatest { criteria ->
            flow {
                if (criteria.isSearchPending) {
                    emit(NotesContentSnapshot(NotesContentState.SearchPending, 0L, false, criteria))
                    return@flow
                }
                // Rank fuzzy matches globally before slicing; browsing uses a bounded Room query.
                val source = if (criteria.appliedQuery.isEmpty()) {
                    notesRepository.observeNotesWindow(criteria.visibleLimit + 1, criteria.sortOrder)
                } else {
                    notesRepository.observeNotes()
                }
                emitAll(source.map { notes ->
                    val previous = lastObservation.getAndSet(criteria to notes)
                        ?.takeIf { it.first == criteria }?.second
                    val previousById = previous?.associateBy(Note::id).orEmpty()
                    val matches = noteListProcessor.process(notes, criteria.appliedQuery, criteria.sortOrder)
                    val content = when {
                        matches.isNotEmpty() -> NotesContentState.Content(
                            notes = matches.take(criteria.visibleLimit),
                            hasMore = matches.size > criteria.visibleLimit,
                            visibleLimit = criteria.visibleLimit,
                        )
                        criteria.appliedQuery.isNotEmpty() -> NotesContentState.SearchEmpty
                        else -> NotesContentState.Empty
                    }
                    NotesContentSnapshot(
                        content = content,
                        notesRevision = notesRevision.incrementAndGet(),
                        scrollToStart = previous != null && notes != previous &&
                            notes.size >= previous.size &&
                            (notes.any { note -> previousById[note.id]?.let { it != note } == true } ||
                                notes.firstOrNull()?.id?.let { it !in previousById } == true),
                        criteria = criteria,
                    )
                })
            }.onStart {
                // Keep the current cards and scroll position while appending a page.
                if (criteria.visibleLimit == NOTES_PAGE_SIZE) {
                    emit(NotesContentSnapshot(NotesContentState.Loading, 0L, false, criteria))
                }
            }.catch {
                emit(NotesContentSnapshot(NotesContentState.Error, 0L, false, criteria))
            }
        }
        .flowOn(searchDispatcher)

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
