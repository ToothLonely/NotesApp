package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.toothlonely.notesapp.feature.notes.impl.domain.NewNote
import dev.toothlonely.notesapp.feature.notes.impl.domain.NoteTitleGenerator
import dev.toothlonely.notesapp.feature.notes.impl.domain.NotesRepository
import dev.toothlonely.notesapp.feature.notes.impl.domain.ResolvedNoteTitle
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NoteEditorViewModel(
    private val notesRepository: NotesRepository,
    private val noteTitleGenerator: NoteTitleGenerator,
) : ViewModel() {
    private val _state = MutableStateFlow<NoteEditorUiState>(NoteEditorUiState.Loading)
    val state: StateFlow<NoteEditorUiState> = _state.asStateFlow()

    private val eventChannel = Channel<NoteEditorEvent>(capacity = Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        prepareNewNote()
    }

    fun onTitleChanged(title: String) {
        _state.update { state ->
            (state as? NoteEditorUiState.Content)?.copy(
                title = title,
                resolvedGeneratedTitleNumber = null,
                hasSaveError = false,
            ) ?: state
        }
    }

    fun onBodyChanged(body: String) {
        _state.update { state ->
            (state as? NoteEditorUiState.Content)?.copy(
                body = body,
                hasSaveError = false,
            ) ?: state
        }
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
                notesRepository.createNote(
                    NewNote(
                        title = resolvedTitle.value,
                        content = content.body,
                        generatedTitleNumber = resolvedTitle.generatedTitleNumber,
                    ),
                )
            }.onSuccess {
                eventChannel.send(NoteEditorEvent.SaveSucceeded)
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
        if (_state.value is NoteEditorUiState.Error) prepareNewNote()
    }

    private fun prepareNewNote() {
        _state.value = NoteEditorUiState.Loading
        viewModelScope.launch {
            runCatching { notesRepository.nextGeneratedTitleNumber() }
                .onSuccess { number ->
                    _state.value = NoteEditorUiState.Content(
                        title = "",
                        body = "",
                        generatedTitleNumber = number,
                    )
                }
                .onFailure {
                    _state.value = NoteEditorUiState.Error
                }
        }
    }
}
