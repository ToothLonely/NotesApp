package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.toothlonely.notesapp.feature.notes.impl.domain.NewNote
import dev.toothlonely.notesapp.feature.notes.impl.domain.NoteTitleGenerator
import dev.toothlonely.notesapp.feature.notes.impl.domain.NoteUpdate
import dev.toothlonely.notesapp.feature.notes.impl.domain.NotesRepository
import dev.toothlonely.notesapp.feature.notes.impl.domain.ResolvedNoteTitle
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NoteEditorViewModel(
    private val args: NoteEditorArgs,
    private val notesRepository: NotesRepository,
    private val noteTitleGenerator: NoteTitleGenerator,
) : ViewModel() {
    private var savedContent: SavedContent? = null

    private val _state = MutableStateFlow<NoteEditorUiState>(
        NoteEditorUiState.Loading(isExistingNote = args.noteId != null),
    )
    val state: StateFlow<NoteEditorUiState> = _state.asStateFlow()

    private val eventChannel = Channel<NoteEditorEvent>(capacity = Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        prepareEditor()
    }

    fun onTitleChanged(title: String) {
        _state.update { state ->
            val content = state as? NoteEditorUiState.Content
            if (content == null || content.mode == NoteEditorMode.Reading || content.isSaving) {
                return@update state
            }
            content.copy(
                title = title,
                resolvedGeneratedTitleNumber = null,
                hasSaveError = false,
            )
        }
    }

    fun onBodyChanged(body: String) {
        _state.update { state ->
            val content = state as? NoteEditorUiState.Content
            if (content == null || content.mode == NoteEditorMode.Reading || content.isSaving) {
                return@update state
            }
            content.copy(
                body = body,
                hasSaveError = false,
            )
        }
    }

    fun startEditing() {
        val content = _state.value as? NoteEditorUiState.Content ?: return
        if (content.mode != NoteEditorMode.Reading) return

        savedContent = SavedContent(
            title = content.title,
            body = content.body,
            resolvedGeneratedTitleNumber = content.resolvedGeneratedTitleNumber,
        )
        _state.value = content.copy(
            mode = NoteEditorMode.Editing,
            hasSaveError = false,
        )
    }

    fun cancelEditing(): Boolean {
        val content = _state.value as? NoteEditorUiState.Content ?: return false
        if (content.mode != NoteEditorMode.Editing) return false
        if (content.isSaving) return true

        val snapshot = savedContent ?: return false
        _state.value = content.copy(
            mode = NoteEditorMode.Reading,
            title = snapshot.title,
            body = snapshot.body,
            resolvedGeneratedTitleNumber = snapshot.resolvedGeneratedTitleNumber,
            isSaving = false,
            hasSaveError = false,
        )
        return true
    }

    fun save() {
        val content = _state.value as? NoteEditorUiState.Content ?: return
        if (!content.isSaveEnabled) return

        val resolvedTitle = content.resolvedGeneratedTitleNumber
            ?.takeIf { number -> content.title == noteTitleGenerator.suggestedTitle(number) }
            ?.let { number ->
                ResolvedNoteTitle(
                    value = content.title,
                    generatedTitleNumber = number,
                )
            }
            ?: noteTitleGenerator.resolve(
                input = content.title,
                number = content.generatedTitleNumber,
            )
        _state.value = content.copy(
            title = resolvedTitle.value,
            resolvedGeneratedTitleNumber = resolvedTitle.generatedTitleNumber,
            isSaving = true,
            hasSaveError = false,
        )

        viewModelScope.launch {
            runCatching {
                val noteId = args.noteId
                if (noteId == null) {
                    notesRepository.createNote(
                        NewNote(
                            title = resolvedTitle.value,
                            content = content.body,
                            generatedTitleNumber = resolvedTitle.generatedTitleNumber,
                        ),
                    )
                } else {
                    check(
                        notesRepository.updateNote(
                            NoteUpdate(
                                id = noteId,
                                title = resolvedTitle.value,
                                content = content.body,
                                generatedTitleNumber = resolvedTitle.generatedTitleNumber,
                            ),
                        ),
                    )
                }
            }.onSuccess {
                if (args.noteId == null) {
                    eventChannel.send(NoteEditorEvent.SaveSucceeded)
                } else {
                    savedContent = SavedContent(
                        title = resolvedTitle.value,
                        body = content.body,
                        resolvedGeneratedTitleNumber = resolvedTitle.generatedTitleNumber,
                    )
                    _state.value = content.copy(
                        mode = NoteEditorMode.Reading,
                        title = resolvedTitle.value,
                        resolvedGeneratedTitleNumber = resolvedTitle.generatedTitleNumber,
                        isSaving = false,
                        hasSaveError = false,
                    )
                }
            }.onFailure {
                _state.update { state ->
                    (state as? NoteEditorUiState.Content)?.copy(
                        isSaving = false,
                        hasSaveError = true,
                    ) ?: state
                }
            }
        }
    }

    fun retryPreparation() {
        if (_state.value is NoteEditorUiState.Error) prepareEditor()
    }

    private fun prepareEditor() {
        val noteId = args.noteId
        savedContent = null
        _state.value = NoteEditorUiState.Loading(isExistingNote = noteId != null)
        viewModelScope.launch {
            runCatching { loadPreparedNote(noteId) }
                .onSuccess { preparedNote ->
                    _state.value = if (preparedNote == null) {
                        savedContent = null
                        NoteEditorUiState.NotFound
                    } else {
                        val mode = if (noteId == null) {
                            NoteEditorMode.Creating
                        } else {
                            NoteEditorMode.Reading
                        }
                        if (mode == NoteEditorMode.Reading) {
                            savedContent = SavedContent(
                                title = preparedNote.title,
                                body = preparedNote.body,
                                resolvedGeneratedTitleNumber =
                                    preparedNote.resolvedGeneratedTitleNumber,
                            )
                        }
                        NoteEditorUiState.Content(
                            mode = mode,
                            title = preparedNote.title,
                            body = preparedNote.body,
                            generatedTitleNumber = preparedNote.generatedTitleNumber,
                            resolvedGeneratedTitleNumber =
                                preparedNote.resolvedGeneratedTitleNumber,
                        )
                    }
                }
                .onFailure {
                    _state.value = NoteEditorUiState.Error
                }
        }
    }

    private suspend fun loadPreparedNote(noteId: Long?): PreparedNote? = if (noteId == null) {
        PreparedNote(
            title = "",
            body = "",
            generatedTitleNumber = notesRepository.nextGeneratedTitleNumber(),
            resolvedGeneratedTitleNumber = null,
        )
    } else {
        notesRepository.observeNote(noteId).first()?.let { note ->
            PreparedNote(
                title = note.title,
                body = note.content,
                generatedTitleNumber = note.generatedTitleNumber
                    ?: notesRepository.nextGeneratedTitleNumber(),
                resolvedGeneratedTitleNumber = note.generatedTitleNumber,
            )
        }
    }

    private data class PreparedNote(
        val title: String,
        val body: String,
        val generatedTitleNumber: Int,
        val resolvedGeneratedTitleNumber: Int?,
    )

    private data class SavedContent(
        val title: String,
        val body: String,
        val resolvedGeneratedTitleNumber: Int?,
    )
}
