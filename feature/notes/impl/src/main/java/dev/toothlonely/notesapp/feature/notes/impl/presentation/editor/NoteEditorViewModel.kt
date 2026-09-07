package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.toothlonely.notesapp.feature.notes.impl.domain.NewNote
import dev.toothlonely.notesapp.feature.notes.impl.domain.NoteImageStorage
import dev.toothlonely.notesapp.feature.notes.impl.domain.NoteImageStorageException
import dev.toothlonely.notesapp.feature.notes.impl.domain.NoteImageStorageFailure
import dev.toothlonely.notesapp.feature.notes.impl.domain.NoteImageUpdate
import dev.toothlonely.notesapp.feature.notes.impl.domain.NoteTitleGenerator
import dev.toothlonely.notesapp.feature.notes.impl.domain.NoteUpdate
import dev.toothlonely.notesapp.feature.notes.impl.domain.NotesRepository
import dev.toothlonely.notesapp.feature.notes.impl.domain.ResolvedNoteTitle
import kotlinx.coroutines.CancellationException
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
    private val imageStorage: NoteImageStorage,
) : ViewModel() {
    private var savedContent: SavedContent? = null
    private var nextImageOperationId = 0L
    private var activeImageOperationId: Long? = null

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
            if (
                content == null ||
                content.mode == NoteEditorMode.Reading ||
                content.isSaving ||
                content.isClosing
            ) {
                return@update state
            }
            content.copy(
                title = title,
                resolvedGeneratedTitleNumber = null,
                saveError = null,
            )
        }
    }

    fun onBodyChanged(body: String) {
        _state.update { state ->
            val content = state as? NoteEditorUiState.Content
            if (
                content == null ||
                content.mode == NoteEditorMode.Reading ||
                content.isSaving ||
                content.isClosing
            ) {
                return@update state
            }
            content.copy(
                body = body,
                saveError = null,
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
            image = content.image as? EditorImage.Persisted,
        )
        _state.value = content.copy(
            mode = NoteEditorMode.Editing,
            saveError = null,
        )
    }

    fun cancelEditing(): Boolean {
        val content = _state.value as? NoteEditorUiState.Content ?: return false
        if (content.mode != NoteEditorMode.Editing) return false
        if (content.isSaving || content.isClosing) return true

        val snapshot = savedContent ?: return false
        activeImageOperationId = null
        discardStagedImage(content)
        _state.value = content.copy(
            mode = NoteEditorMode.Reading,
            title = snapshot.title,
            body = snapshot.body,
            resolvedGeneratedTitleNumber = snapshot.resolvedGeneratedTitleNumber,
            image = snapshot.image,
            isProcessingImage = false,
            isClosing = false,
            attachmentError = null,
            isSaving = false,
            saveError = null,
        )
        return true
    }

    fun attachImage(
        sourceUri: String,
        disposableSourceFileName: String? = null,
    ) {
        val content = _state.value as? NoteEditorUiState.Content ?: return
        if (
            content.mode == NoteEditorMode.Reading ||
            content.isSaving ||
            content.isProcessingImage ||
            content.isClosing
        ) {
            disposableSourceFileName?.let(::discardStagedFile)
            return
        }
        val operationId = ++nextImageOperationId
        activeImageOperationId = operationId
        _state.value = content.copy(
            isProcessingImage = true,
            attachmentError = null,
            saveError = null,
        )
        viewModelScope.launch {
            stageImage(
                operationId = operationId,
                sourceUri = sourceUri,
                disposableSourceFileName = disposableSourceFileName,
            )
        }
    }

    fun removeImage() {
        val content = _state.value as? NoteEditorUiState.Content ?: return
        if (
            content.mode == NoteEditorMode.Reading ||
            content.isSaving ||
            content.isProcessingImage ||
            content.isClosing
        ) return
        discardStagedImage(content)
        _state.value = content.copy(
            image = null,
            attachmentError = null,
            saveError = null,
        )
    }

    fun dismissAttachmentError() {
        _state.update { state ->
            (state as? NoteEditorUiState.Content)?.copy(attachmentError = null) ?: state
        }
    }

    fun discardCameraCapture(stagingFileName: String) {
        discardStagedFile(stagingFileName)
    }

    fun onCameraPreparationFailed() {
        _state.update { state ->
            val content = state as? NoteEditorUiState.Content ?: return@update state
            if (content.mode == NoteEditorMode.Reading || content.isClosing) return@update state
            content.copy(
                attachmentError = NoteEditorAttachmentError.WriteFailed,
            )
        }
    }

    fun onImageLoadFailed(fileName: String, staged: Boolean) {
        _state.update { state ->
            val content = state as? NoteEditorUiState.Content ?: return@update state
            val failedImage = if (staged) {
                EditorImage.Staged(fileName)
            } else {
                EditorImage.Persisted(fileName)
            }
            if (content.image != failedImage) return@update state
            content.copy(
                attachmentError = if (failedImage is EditorImage.Staged) {
                    NoteEditorAttachmentError.SelectedImageUnavailable
                } else {
                    NoteEditorAttachmentError.StoredImageUnavailable
                },
            )
        }
    }

    fun onBack() {
        val content = _state.value as? NoteEditorUiState.Content
        if (content?.isSaving == true || content?.isClosing == true) return
        if (cancelEditing()) return
        if (
            content?.mode == NoteEditorMode.Creating &&
            content.isProcessingImage
        ) {
            _state.value = content.copy(
                isClosing = true,
                attachmentError = null,
                saveError = null,
            )
            return
        }

        viewModelScope.launch {
            if (content?.mode == NoteEditorMode.Creating) {
                (content.image as? EditorImage.Staged)?.fileName?.let { fileName ->
                    discardStagedFileSafely(fileName)
                }
            }
            eventChannel.send(NoteEditorEvent.CloseEditor)
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
            saveError = null,
        )

        viewModelScope.launch {
            saveContent(content, resolvedTitle)
        }
    }

    fun retryPreparation() {
        if (_state.value is NoteEditorUiState.Error) prepareEditor()
    }

    private fun prepareEditor() {
        val noteId = args.noteId
        savedContent = null
        activeImageOperationId = null
        _state.value = NoteEditorUiState.Loading(isExistingNote = noteId != null)
        viewModelScope.launch {
            loadEditor(noteId)
        }
    }

    private suspend fun stageImage(
        operationId: Long,
        sourceUri: String,
        disposableSourceFileName: String?,
    ) {
        val stagedFileName = try {
            imageStorage.stageImage(
                sourceUri = sourceUri,
                disposableSourceFileName = disposableSourceFileName,
            )
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            handleImageStagingFailure(operationId, error)
            return
        }

        if (activeImageOperationId != operationId) {
            discardStagedFileSafely(stagedFileName)
            return
        }

        val current = _state.value as? NoteEditorUiState.Content
        if (current?.isClosing == true) {
            closeEditorAfterImageProcessing(operationId, stagedFileName)
            return
        }
        if (
            current == null ||
            current.mode == NoteEditorMode.Reading ||
            current.isSaving ||
            !current.isProcessingImage
        ) {
            activeImageOperationId = null
            discardStagedFileSafely(stagedFileName)
            return
        }

        val oldStagedFileName = (current.image as? EditorImage.Staged)?.fileName
        if (oldStagedFileName != null && oldStagedFileName != stagedFileName) {
            discardStagedFileSafely(oldStagedFileName)
        }
        activeImageOperationId = null
        _state.value = current.copy(
            image = EditorImage.Staged(stagedFileName),
            isProcessingImage = false,
            attachmentError = null,
        )
    }

    private suspend fun handleImageStagingFailure(
        operationId: Long,
        error: Throwable,
    ) {
        if (activeImageOperationId != operationId) return
        val current = _state.value as? NoteEditorUiState.Content
        if (current?.isClosing == true) {
            closeEditorAfterImageProcessing(operationId)
            return
        }
        activeImageOperationId = null
        if (
            current == null ||
            current.mode == NoteEditorMode.Reading ||
            !current.isProcessingImage
        ) return
        _state.value = current.copy(
            isProcessingImage = false,
            attachmentError = error.asAttachmentError(),
        )
    }

    private suspend fun closeEditorAfterImageProcessing(
        operationId: Long,
        newStagedFileName: String? = null,
    ) {
        if (activeImageOperationId != operationId) return
        activeImageOperationId = null
        newStagedFileName?.let { fileName ->
            discardStagedFileSafely(fileName)
        }
        val current = _state.value as? NoteEditorUiState.Content
        val oldStagedFileName = (current?.image as? EditorImage.Staged)?.fileName
        if (oldStagedFileName != null && oldStagedFileName != newStagedFileName) {
            discardStagedFileSafely(oldStagedFileName)
        }
        eventChannel.send(NoteEditorEvent.CloseEditor)
    }

    private suspend fun saveContent(
        content: NoteEditorUiState.Content,
        resolvedTitle: ResolvedNoteTitle,
    ) {
        try {
            val noteId = args.noteId
            if (noteId == null) {
                notesRepository.createNote(
                    NewNote(
                        title = resolvedTitle.value,
                        content = content.body,
                        generatedTitleNumber = resolvedTitle.generatedTitleNumber,
                        stagedImageFileName =
                            (content.image as? EditorImage.Staged)?.fileName,
                    ),
                )
                eventChannel.send(NoteEditorEvent.SaveSucceeded)
            } else {
                check(
                    notesRepository.updateNote(
                        NoteUpdate(
                            id = noteId,
                            title = resolvedTitle.value,
                            content = content.body,
                            generatedTitleNumber = resolvedTitle.generatedTitleNumber,
                            imageUpdate = content.asImageUpdate(),
                        ),
                    ),
                )
                val persistedImage = content.image?.fileName?.let(EditorImage::Persisted)
                savedContent = SavedContent(
                    title = resolvedTitle.value,
                    body = content.body,
                    resolvedGeneratedTitleNumber = resolvedTitle.generatedTitleNumber,
                    image = persistedImage,
                )
                _state.value = content.copy(
                    mode = NoteEditorMode.Reading,
                    title = resolvedTitle.value,
                    resolvedGeneratedTitleNumber = resolvedTitle.generatedTitleNumber,
                    image = persistedImage,
                    isSaving = false,
                    saveError = null,
                )
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            (content.image as? EditorImage.Staged)?.fileName?.let(::discardStagedFile)
            _state.update { state ->
                (state as? NoteEditorUiState.Content)?.copy(
                    image = savedContent?.image,
                    isSaving = false,
                    saveError = if (error is NoteImageStorageException) {
                        NoteEditorSaveError.Image
                    } else {
                        NoteEditorSaveError.Note
                    },
                ) ?: state
            }
        }
    }

    private suspend fun loadEditor(noteId: Long?) {
        try {
            val preparedNote = loadPreparedNote(noteId)
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
                        image = preparedNote.image,
                    )
                }
                NoteEditorUiState.Content(
                    mode = mode,
                    title = preparedNote.title,
                    body = preparedNote.body,
                    generatedTitleNumber = preparedNote.generatedTitleNumber,
                    resolvedGeneratedTitleNumber = preparedNote.resolvedGeneratedTitleNumber,
                    image = preparedNote.image,
                )
            }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Throwable) {
            _state.value = NoteEditorUiState.Error
        }
    }

    private suspend fun loadPreparedNote(noteId: Long?): PreparedNote? = if (noteId == null) {
        PreparedNote(
            title = "",
            body = "",
            generatedTitleNumber = notesRepository.nextGeneratedTitleNumber(),
            resolvedGeneratedTitleNumber = null,
            image = null,
        )
    } else {
        notesRepository.observeNote(noteId).first()?.let { note ->
            PreparedNote(
                title = note.title,
                body = note.content,
                generatedTitleNumber = note.generatedTitleNumber
                    ?: notesRepository.nextGeneratedTitleNumber(),
                resolvedGeneratedTitleNumber = note.generatedTitleNumber,
                image = note.imageFileName?.let(EditorImage::Persisted),
            )
        }
    }

    private data class PreparedNote(
        val title: String,
        val body: String,
        val generatedTitleNumber: Int,
        val resolvedGeneratedTitleNumber: Int?,
        val image: EditorImage.Persisted?,
    )

    private data class SavedContent(
        val title: String,
        val body: String,
        val resolvedGeneratedTitleNumber: Int?,
        val image: EditorImage.Persisted?,
    )

    private fun NoteEditorUiState.Content.asImageUpdate(): NoteImageUpdate = when (val image = image) {
        is EditorImage.Staged -> NoteImageUpdate.Replace(image.fileName)
        is EditorImage.Persisted -> NoteImageUpdate.Keep
        null -> if (savedContent?.image != null) NoteImageUpdate.Remove else NoteImageUpdate.Keep
    }

    private fun discardStagedImage(content: NoteEditorUiState.Content) {
        (content.image as? EditorImage.Staged)?.fileName?.let(::discardStagedFile)
    }

    private fun discardStagedFile(fileName: String) {
        viewModelScope.launch {
            discardStagedFileSafely(fileName)
        }
    }

    private suspend fun discardStagedFileSafely(fileName: String) {
        try {
            imageStorage.discardStaged(fileName)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Throwable) {
            Unit
        }
    }

    private fun Throwable.asAttachmentError(): NoteEditorAttachmentError =
        when ((this as? NoteImageStorageException)?.failure) {
            NoteImageStorageFailure.InputTooLarge -> NoteEditorAttachmentError.InputTooLarge
            NoteImageStorageFailure.UnsupportedImage -> NoteEditorAttachmentError.UnsupportedImage
            NoteImageStorageFailure.WriteFailed -> NoteEditorAttachmentError.WriteFailed
            NoteImageStorageFailure.SourceUnavailable,
            null,
                -> NoteEditorAttachmentError.SelectedImageUnavailable
        }
}
