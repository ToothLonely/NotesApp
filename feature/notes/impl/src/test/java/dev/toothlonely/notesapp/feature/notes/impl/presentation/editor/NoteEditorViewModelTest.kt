package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor

import dev.toothlonely.notesapp.feature.notes.impl.testutil.FakeNotesRepository
import dev.toothlonely.notesapp.feature.notes.impl.testutil.FakeNoteImageStorage
import dev.toothlonely.notesapp.feature.notes.impl.testutil.FakeSpeechRecognitionRepository
import dev.toothlonely.notesapp.core.domain.speech.SpeechRecognitionEvent
import dev.toothlonely.notesapp.core.domain.speech.SpeechRecognitionFailure
import dev.toothlonely.notesapp.feature.notes.impl.testutil.MainDispatcherRule
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NewNote
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.Note
import dev.toothlonely.notesapp.feature.notes.impl.domain.repository.NoteImageStorageException
import dev.toothlonely.notesapp.feature.notes.impl.domain.repository.NoteImageStorageFailure
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NoteImageUpdate
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NoteUpdate
import dev.toothlonely.notesapp.feature.notes.impl.domain.usecase.NoteTitleGenerator
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.model.EditorImage
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.model.NoteEditorArgs
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.model.NoteEditorMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteEditorViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val titleGenerator = NoteTitleGenerator("Заметка %d")
    @Test
    fun `initial state is loading then new content is prepared`() = runTest {
        val viewModel = createViewModel(FakeNotesRepository())

        assertEquals(
            NoteEditorUiState.Loading(isExistingNote = false),
            viewModel.state.value,
        )
        runCurrent()

        val state = viewModel.state.value
        assertEquals(
            NoteEditorUiState.Content(
                title = "",
                body = "",
                generatedTitleNumber = 1,
            ),
            state,
        )
        assertFalse((state as NoteEditorUiState.Content).isSaveEnabled)
    }

    @Test
    fun `title and body changes update content`() = runTest {
        val viewModel = createViewModel(FakeNotesRepository())
        runCurrent()

        viewModel.onTitleChanged("Заголовок")
        viewModel.onBodyChanged("Текст")

        val state = viewModel.state.value as NoteEditorUiState.Content
        assertEquals("Заголовок", state.title)
        assertEquals("Текст", state.body)
        assertTrue(state.isSaveEnabled)
    }

    @Test
    fun `save is enabled when either title or body is filled`() = runTest {
        val repository = FakeNotesRepository()
        val viewModel = createViewModel(repository)
        runCurrent()

        viewModel.onTitleChanged("   ")
        viewModel.onBodyChanged("Текст")
        assertTrue((viewModel.state.value as NoteEditorUiState.Content).isSaveEnabled)

        viewModel.onTitleChanged("Заголовок")
        viewModel.onBodyChanged("   ")
        assertTrue((viewModel.state.value as NoteEditorUiState.Content).isSaveEnabled)
    }

    @Test
    fun `save is ignored when title and body are blank`() = runTest {
        val repository = FakeNotesRepository()
        val viewModel = createViewModel(repository)
        runCurrent()

        viewModel.onTitleChanged("   ")
        viewModel.onBodyChanged("   ")
        viewModel.save()

        assertFalse((viewModel.state.value as NoteEditorUiState.Content).isSaveEnabled)
        assertTrue(repository.createdNotes.isEmpty())
    }

    @Test
    fun `body-only note uses generated title and emits completion`() = runTest {
        val repository = FakeNotesRepository().apply { nextTitleNumber = 4 }
        val viewModel = createViewModel(repository)
        runCurrent()
        viewModel.onBodyChanged("Текст")
        val event = async { viewModel.events.first() }

        viewModel.save()
        assertTrue((viewModel.state.value as NoteEditorUiState.Content).isSaving)
        runCurrent()

        assertEquals(NoteEditorEvent.SaveSucceeded, event.await())
        assertEquals(
            listOf(NewNote("Заметка 4", "Текст", generatedTitleNumber = 4)),
            repository.createdNotes,
        )
    }

    @Test
    fun `title-only note is saved with empty body`() = runTest {
        val repository = FakeNotesRepository()
        val viewModel = createViewModel(repository)
        runCurrent()
        viewModel.onTitleChanged("  Заголовок  ")
        val event = async { viewModel.events.first() }

        viewModel.save()
        runCurrent()

        assertEquals(NoteEditorEvent.SaveSucceeded, event.await())
        assertEquals(
            NewNote("Заголовок", "", generatedTitleNumber = null),
            repository.createdNotes.single(),
        )
    }

    @Test
    fun `save failure keeps input and exposes retryable error`() = runTest {
        val repository = FakeNotesRepository().apply {
            saveFailure = IllegalStateException("Write failed")
        }
        val viewModel = createViewModel(repository)
        runCurrent()
        viewModel.onTitleChanged("Заголовок")
        viewModel.onBodyChanged("Текст")

        viewModel.save()
        runCurrent()

        val state = viewModel.state.value as NoteEditorUiState.Content
        assertEquals("Заголовок", state.title)
        assertEquals("Текст", state.body)
        assertFalse(state.isSaving)
        assertEquals(NoteEditorSaveError.Note, state.saveError)
    }

    @Test
    fun `retry after save failure preserves entered content`() = runTest {
        val repository = FakeNotesRepository().apply {
            saveFailure = IllegalStateException("Write failed")
        }
        val viewModel = createViewModel(repository)
        runCurrent()
        viewModel.onTitleChanged("Заголовок")
        viewModel.onBodyChanged("Текст")

        viewModel.save()
        runCurrent()
        repository.saveFailure = null
        val event = async { viewModel.events.first() }

        viewModel.save()
        runCurrent()

        assertEquals(NoteEditorEvent.SaveSucceeded, event.await())
        assertEquals(
            NewNote("Заголовок", "Текст", generatedTitleNumber = null),
            repository.createdNotes.single(),
        )
    }

    @Test
    fun `existing note moves from loading to populated content`() = runTest {
        val repository = FakeNotesRepository().apply {
            notes.value = listOf(
                Note(
                    id = 7,
                    title = "Сохранённый заголовок",
                    content = "Сохранённый текст",
                    createdAtMillis = 123,
                    generatedTitleNumber = null,
                ),
            )
            nextTitleNumber = 9
        }
        val viewModel = createViewModel(repository, noteId = 7)

        assertEquals(
            NoteEditorUiState.Loading(isExistingNote = true),
            viewModel.state.value,
        )
        runCurrent()

        assertEquals(
            NoteEditorUiState.Content(
                mode = NoteEditorMode.Reading,
                title = "Сохранённый заголовок",
                body = "Сохранённый текст",
                generatedTitleNumber = 9,
            ),
            viewModel.state.value,
        )
    }

    @Test
    fun `missing existing note shows not found`() = runTest {
        val viewModel = createViewModel(FakeNotesRepository(), noteId = 404)

        runCurrent()

        assertEquals(NoteEditorUiState.NotFound, viewModel.state.value)
    }

    @Test
    fun `load failure shows error and retry loads existing note`() = runTest {
        val repository = FakeNotesRepository().apply {
            observeNoteFailure = IllegalStateException("Read failed")
        }
        val viewModel = createViewModel(repository, noteId = 5)
        runCurrent()
        assertEquals(NoteEditorUiState.Error, viewModel.state.value)

        repository.observeNoteFailure = null
        repository.notes.value = listOf(Note(5, "Заголовок", "Текст", 100))
        viewModel.retryPreparation()
        runCurrent()

        val state = viewModel.state.value as NoteEditorUiState.Content
        assertEquals(NoteEditorMode.Reading, state.mode)
        assertEquals("Заголовок", state.title)
        assertEquals("Текст", state.body)
    }

    @Test
    fun `edit action switches existing note from reading to editing`() = runTest {
        val repository = FakeNotesRepository().apply {
            notes.value = listOf(Note(3, "Заголовок", "Текст", 777))
        }
        val viewModel = createViewModel(repository, noteId = 3)
        runCurrent()

        viewModel.startEditing()

        val state = viewModel.state.value as NoteEditorUiState.Content
        assertEquals(NoteEditorMode.Editing, state.mode)
        assertEquals("Заголовок", state.title)
        assertEquals("Текст", state.body)
        assertTrue(state.isSaveEnabled)
    }

    @Test
    fun `reading mode ignores changes and save`() = runTest {
        val repository = FakeNotesRepository().apply {
            notes.value = listOf(Note(3, "Заголовок", "Текст", 777))
        }
        val viewModel = createViewModel(repository, noteId = 3)
        runCurrent()

        viewModel.onTitleChanged("Другой заголовок")
        viewModel.onBodyChanged("Другой текст")
        viewModel.save()
        runCurrent()

        val state = viewModel.state.value as NoteEditorUiState.Content
        assertEquals(NoteEditorMode.Reading, state.mode)
        assertEquals("Заголовок", state.title)
        assertEquals("Текст", state.body)
        assertFalse(state.isSaveEnabled)
        assertTrue(repository.updatedNotes.isEmpty())
    }

    @Test
    fun `back from editing discards changes and returns to reading`() = runTest {
        val repository = FakeNotesRepository().apply {
            notes.value = listOf(Note(3, "Заголовок", "Текст", 777))
        }
        val viewModel = createViewModel(repository, noteId = 3)
        runCurrent()
        viewModel.startEditing()
        viewModel.onTitleChanged("Другой заголовок")
        viewModel.onBodyChanged("Другой текст")

        assertTrue(viewModel.cancelEditing())

        val state = viewModel.state.value as NoteEditorUiState.Content
        assertEquals(NoteEditorMode.Reading, state.mode)
        assertEquals("Заголовок", state.title)
        assertEquals("Текст", state.body)
        assertEquals(null, state.saveError)
        assertTrue(repository.updatedNotes.isEmpty())
    }

    @Test
    fun `editing existing note updates it without creating a duplicate and preserves creation time`() =
        runTest {
            val repository = FakeNotesRepository().apply {
                notes.value = listOf(Note(3, "Старый", "Старый текст", 777))
                currentTimeMillis = 999
            }
            val viewModel = createViewModel(repository, noteId = 3)
            runCurrent()
            viewModel.startEditing()
            viewModel.onTitleChanged("Новый")
            viewModel.onBodyChanged("Новый текст")

            viewModel.save()
            runCurrent()

            assertTrue(repository.createdNotes.isEmpty())
            assertEquals(
                NoteUpdate(3, "Новый", "Новый текст", generatedTitleNumber = null),
                repository.updatedNotes.single(),
            )
            assertEquals(777, repository.notes.value.single().createdAtMillis)
            assertEquals(999, repository.notes.value.single().updatedAtMillis)
            assertEquals(1, repository.notes.value.size)
            val state = viewModel.state.value as NoteEditorUiState.Content
            assertEquals(NoteEditorMode.Reading, state.mode)
            assertEquals("Новый", state.title)
            assertEquals("Новый текст", state.body)
        }

    @Test
    fun `body-only update uses fallback title`() = runTest {
        val repository = FakeNotesRepository().apply {
            notes.value = listOf(Note(3, "Исходный", "Текст", 777))
            nextTitleNumber = 8
        }
        val viewModel = createViewModel(repository, noteId = 3)
        runCurrent()
        viewModel.startEditing()
        viewModel.onTitleChanged("   ")
        viewModel.onBodyChanged("Обновлённый текст")

        viewModel.save()
        runCurrent()

        assertEquals(
            NoteUpdate(3, "Заметка 8", "Обновлённый текст", generatedTitleNumber = 8),
            repository.updatedNotes.single(),
        )
    }

    @Test
    fun `unchanged generated title keeps its persistent number during update`() = runTest {
        val repository = FakeNotesRepository().apply {
            notes.value = listOf(
                Note(
                    id = 3,
                    title = "Заметка 4",
                    content = "Текст",
                    createdAtMillis = 777,
                    generatedTitleNumber = 4,
                ),
            )
            nextTitleNumber = 5
        }
        val viewModel = createViewModel(repository, noteId = 3)
        runCurrent()
        viewModel.startEditing()
        viewModel.onBodyChanged("Обновлённый текст")

        viewModel.save()
        runCurrent()

        assertEquals(
            NoteUpdate(3, "Заметка 4", "Обновлённый текст", generatedTitleNumber = 4),
            repository.updatedNotes.single(),
        )
    }

    @Test
    fun `fully blank existing note is not updated`() = runTest {
        val repository = FakeNotesRepository().apply {
            notes.value = listOf(Note(3, "Исходный", "Текст", 777))
        }
        val viewModel = createViewModel(repository, noteId = 3)
        runCurrent()
        viewModel.startEditing()
        viewModel.onTitleChanged("   ")
        viewModel.onBodyChanged("   ")

        viewModel.save()
        runCurrent()

        assertTrue(repository.updatedNotes.isEmpty())
        assertFalse((viewModel.state.value as NoteEditorUiState.Content).isSaveEnabled)
    }

    @Test
    fun `update failure keeps input and retry updates the same note`() = runTest {
        val repository = FakeNotesRepository().apply {
            notes.value = listOf(Note(6, "Исходный", "Текст", 900))
            updateFailure = IllegalStateException("Update failed")
        }
        val viewModel = createViewModel(repository, noteId = 6)
        runCurrent()
        viewModel.startEditing()
        viewModel.onTitleChanged("Изменённый")
        viewModel.onBodyChanged("Изменённый текст")

        viewModel.save()
        runCurrent()

        val failedState = viewModel.state.value as NoteEditorUiState.Content
        assertEquals(NoteEditorMode.Editing, failedState.mode)
        assertEquals("Изменённый", failedState.title)
        assertEquals("Изменённый текст", failedState.body)
        assertEquals(NoteEditorSaveError.Note, failedState.saveError)
        assertTrue(repository.createdNotes.isEmpty())
        assertEquals(900, repository.notes.value.single().updatedAtMillis)

        repository.updateFailure = null
        viewModel.save()
        runCurrent()

        assertEquals(1, repository.updatedNotes.size)
        assertEquals("Изменённый", repository.notes.value.single().title)
        assertEquals(900, repository.notes.value.single().createdAtMillis)
        assertEquals(1_000, repository.notes.value.single().updatedAtMillis)
        assertEquals(
            NoteEditorMode.Reading,
            (viewModel.state.value as NoteEditorUiState.Content).mode,
        )
    }

    @Test
    fun `selected image is staged and included when creating note`() = runTest {
        val repository = FakeNotesRepository()
        val imageStorage = FakeNoteImageStorage()
        val viewModel = createViewModel(repository, imageStorage = imageStorage)
        runCurrent()
        viewModel.onTitleChanged("Заголовок")

        viewModel.attachImage("content://picker/image")
        runCurrent()

        val attachedState = viewModel.state.value as NoteEditorUiState.Content
        assertEquals(
            EditorImage.Staged(imageStorage.nextStagedFileName),
            attachedState.image,
        )
        assertEquals(listOf("content://picker/image"), imageStorage.stagedSourceUris)

        val event = async { viewModel.events.first() }
        viewModel.save()
        runCurrent()

        assertEquals(NoteEditorEvent.SaveSucceeded, event.await())
        assertEquals(
            imageStorage.nextStagedFileName,
            repository.createdNotes.single().stagedImageFileName,
        )
    }

    @Test
    fun `selecting a second image discards first staging file`() = runTest {
        val imageStorage = FakeNoteImageStorage()
        val viewModel = createViewModel(
            repository = FakeNotesRepository(),
            imageStorage = imageStorage,
        )
        runCurrent()
        val firstFileName = imageStorage.nextStagedFileName
        viewModel.attachImage("content://picker/first")
        runCurrent()
        imageStorage.nextStagedFileName = "00000000-0000-0000-0000-000000000002.jpg"

        viewModel.attachImage("content://picker/second")
        runCurrent()

        val state = viewModel.state.value as NoteEditorUiState.Content
        assertEquals(EditorImage.Staged(imageStorage.nextStagedFileName), state.image)
        assertEquals(listOf(firstFileName), imageStorage.discardedFileNames)
    }

    @Test
    fun `saved image is loaded with existing note`() = runTest {
        val repository = FakeNotesRepository().apply {
            notes.value = listOf(
                Note(
                    id = 12,
                    title = "С изображением",
                    content = "Текст",
                    createdAtMillis = 100,
                    imageFileName = "saved.jpg",
                ),
            )
        }

        val viewModel = createViewModel(repository, noteId = 12)
        runCurrent()

        val state = viewModel.state.value as NoteEditorUiState.Content
        assertEquals(NoteEditorMode.Reading, state.mode)
        assertEquals(EditorImage.Persisted("saved.jpg"), state.image)
    }

    @Test
    fun `replacing existing image saves replacement and removes staged flag`() = runTest {
        val repository = FakeNotesRepository().apply {
            notes.value = listOf(
                Note(2, "Заголовок", "Текст", 100, imageFileName = "old.jpg"),
            )
        }
        val imageStorage = FakeNoteImageStorage()
        val viewModel = createViewModel(repository, noteId = 2, imageStorage = imageStorage)
        runCurrent()
        viewModel.startEditing()

        viewModel.attachImage("content://picker/replacement")
        runCurrent()
        viewModel.save()
        runCurrent()

        assertEquals(
            NoteImageUpdate.Replace(imageStorage.nextStagedFileName),
            repository.updatedNotes.single().imageUpdate,
        )
        val state = viewModel.state.value as NoteEditorUiState.Content
        assertEquals(NoteEditorMode.Reading, state.mode)
        assertEquals(EditorImage.Persisted(imageStorage.nextStagedFileName), state.image)
    }

    @Test
    fun `removing existing image saves removal`() = runTest {
        val repository = FakeNotesRepository().apply {
            notes.value = listOf(
                Note(2, "Заголовок", "Текст", 100, imageFileName = "old.jpg"),
            )
        }
        val viewModel = createViewModel(repository, noteId = 2)
        runCurrent()
        viewModel.startEditing()

        viewModel.removeImage()
        viewModel.save()
        runCurrent()

        assertEquals(NoteImageUpdate.Remove, repository.updatedNotes.single().imageUpdate)
        assertEquals(null, repository.notes.value.single().imageFileName)
        assertEquals(
            null,
            (viewModel.state.value as NoteEditorUiState.Content).image,
        )
    }

    @Test
    fun `cancelling replacement discards staged file and restores saved image`() = runTest {
        val repository = FakeNotesRepository().apply {
            notes.value = listOf(
                Note(2, "Заголовок", "Текст", 100, imageFileName = "old.jpg"),
            )
        }
        val imageStorage = FakeNoteImageStorage()
        val viewModel = createViewModel(repository, noteId = 2, imageStorage = imageStorage)
        runCurrent()
        viewModel.startEditing()
        viewModel.attachImage("content://picker/replacement")
        runCurrent()

        viewModel.cancelEditing()
        runCurrent()

        val state = viewModel.state.value as NoteEditorUiState.Content
        assertEquals(NoteEditorMode.Reading, state.mode)
        assertEquals(EditorImage.Persisted("old.jpg"), state.image)
        assertEquals(
            listOf(imageStorage.nextStagedFileName),
            imageStorage.discardedFileNames,
        )
        assertTrue(repository.updatedNotes.isEmpty())
    }

    @Test
    fun `failure from cancelled editing image operation is ignored`() = runTest {
        val repository = FakeNotesRepository().apply {
            notes.value = listOf(
                Note(2, "Заголовок", "Текст", 100, imageFileName = "old.jpg"),
            )
        }
        val stageGate = CompletableDeferred<Unit>()
        val imageStorage = FakeNoteImageStorage().apply {
            this.stageGate = stageGate
        }
        val viewModel = createViewModel(repository, noteId = 2, imageStorage = imageStorage)
        runCurrent()
        viewModel.startEditing()
        viewModel.attachImage("content://picker/replacement")
        runCurrent()

        viewModel.cancelEditing()
        imageStorage.stageFailure =
            NoteImageStorageException(NoteImageStorageFailure.UnsupportedImage)
        stageGate.complete(Unit)
        runCurrent()

        val state = viewModel.state.value as NoteEditorUiState.Content
        assertEquals(NoteEditorMode.Reading, state.mode)
        assertEquals(EditorImage.Persisted("old.jpg"), state.image)
        assertEquals(null, state.attachmentError)
        assertFalse(state.isProcessingImage)
    }

    @Test
    fun `stale image load failure is ignored after editing is cancelled`() = runTest {
        val repository = FakeNotesRepository().apply {
            notes.value = listOf(
                Note(2, "Заголовок", "Текст", 100, imageFileName = "old.jpg"),
            )
        }
        val imageStorage = FakeNoteImageStorage()
        val viewModel = createViewModel(repository, noteId = 2, imageStorage = imageStorage)
        runCurrent()
        viewModel.startEditing()
        viewModel.attachImage("content://picker/replacement")
        runCurrent()
        val stagedFileName = imageStorage.nextStagedFileName
        viewModel.cancelEditing()

        viewModel.onImageLoadFailed(stagedFileName, staged = true)

        val state = viewModel.state.value as NoteEditorUiState.Content
        assertEquals(NoteEditorMode.Reading, state.mode)
        assertEquals(EditorImage.Persisted("old.jpg"), state.image)
        assertEquals(null, state.attachmentError)
    }

    @Test
    fun `image load failure distinguishes staged and persisted images`() = runTest {
        val repository = FakeNotesRepository().apply {
            notes.value = listOf(
                Note(2, "Заголовок", "Текст", 100, imageFileName = "old.jpg"),
            )
        }
        val imageStorage = FakeNoteImageStorage()
        val viewModel = createViewModel(repository, noteId = 2, imageStorage = imageStorage)
        runCurrent()

        viewModel.onImageLoadFailed("old.jpg", staged = false)
        assertEquals(
            NoteEditorAttachmentError.StoredImageUnavailable,
            (viewModel.state.value as NoteEditorUiState.Content).attachmentError,
        )

        viewModel.startEditing()
        viewModel.attachImage("content://picker/replacement")
        runCurrent()
        viewModel.onImageLoadFailed(imageStorage.nextStagedFileName, staged = true)
        assertEquals(
            NoteEditorAttachmentError.SelectedImageUnavailable,
            (viewModel.state.value as NoteEditorUiState.Content).attachmentError,
        )
    }

    @Test
    fun `image staging error keeps entered text and previous image`() = runTest {
        val repository = FakeNotesRepository().apply {
            notes.value = listOf(
                Note(2, "Заголовок", "Текст", 100, imageFileName = "old.jpg"),
            )
        }
        val imageStorage = FakeNoteImageStorage().apply {
            stageFailure = NoteImageStorageException(NoteImageStorageFailure.UnsupportedImage)
        }
        val viewModel = createViewModel(repository, noteId = 2, imageStorage = imageStorage)
        runCurrent()
        viewModel.startEditing()
        viewModel.onTitleChanged("Новый заголовок")
        viewModel.onBodyChanged("Новый текст")

        viewModel.attachImage("content://picker/not-image")
        runCurrent()

        val state = viewModel.state.value as NoteEditorUiState.Content
        assertEquals("Новый заголовок", state.title)
        assertEquals("Новый текст", state.body)
        assertEquals(EditorImage.Persisted("old.jpg"), state.image)
        assertEquals(NoteEditorAttachmentError.UnsupportedImage, state.attachmentError)
        assertFalse(state.isProcessingImage)
    }

    @Test
    fun `image save error keeps text and restores persisted attachment`() = runTest {
        val repository = FakeNotesRepository().apply {
            notes.value = listOf(
                Note(2, "Старый", "Старый текст", 100, imageFileName = "old.jpg"),
            )
            updateFailure = NoteImageStorageException(NoteImageStorageFailure.WriteFailed)
        }
        val imageStorage = FakeNoteImageStorage()
        val viewModel = createViewModel(repository, noteId = 2, imageStorage = imageStorage)
        runCurrent()
        viewModel.startEditing()
        viewModel.onTitleChanged("Новый")
        viewModel.onBodyChanged("Новый текст")
        viewModel.attachImage("content://picker/new")
        runCurrent()

        viewModel.save()
        runCurrent()

        val state = viewModel.state.value as NoteEditorUiState.Content
        assertEquals("Новый", state.title)
        assertEquals("Новый текст", state.body)
        assertEquals(EditorImage.Persisted("old.jpg"), state.image)
        assertEquals(NoteEditorSaveError.Image, state.saveError)
    }

    @Test
    fun `back from creation discards staged image before closing`() = runTest {
        val repository = FakeNotesRepository()
        val imageStorage = FakeNoteImageStorage()
        val viewModel = createViewModel(repository, imageStorage = imageStorage)
        runCurrent()
        viewModel.attachImage("content://picker/new")
        runCurrent()
        val event = async { viewModel.events.first() }

        viewModel.onBack()
        runCurrent()

        assertEquals(
            listOf(imageStorage.nextStagedFileName),
            imageStorage.discardedFileNames,
        )
        assertEquals(NoteEditorEvent.CloseEditor, event.await())
        assertTrue(repository.createdNotes.isEmpty())
    }

    @Test
    fun `back during image processing discards result before closing`() = runTest {
        val repository = FakeNotesRepository()
        val stageGate = CompletableDeferred<Unit>()
        val imageStorage = FakeNoteImageStorage().apply {
            this.stageGate = stageGate
        }
        val viewModel = createViewModel(repository, imageStorage = imageStorage)
        runCurrent()
        viewModel.onTitleChanged("До закрытия")
        viewModel.attachImage("content://picker/new")
        runCurrent()
        val event = async { viewModel.events.first() }

        viewModel.onBack()

        val closingState = viewModel.state.value as NoteEditorUiState.Content
        assertTrue(closingState.isClosing)
        assertFalse(closingState.isSaveEnabled)
        viewModel.onTitleChanged("После закрытия")
        viewModel.onBodyChanged("Новый текст")
        val stateAfterInput = viewModel.state.value as NoteEditorUiState.Content
        assertEquals("До закрытия", stateAfterInput.title)
        assertEquals("", stateAfterInput.body)

        stageGate.complete(Unit)
        advanceUntilIdle()

        assertEquals(
            listOf(imageStorage.nextStagedFileName),
            imageStorage.discardedFileNames,
        )
        assertEquals(NoteEditorEvent.CloseEditor, event.await())
        assertTrue(repository.createdNotes.isEmpty())
    }

    @Test
    fun `voice result is appended to existing body and recording blocks save`() = runTest {
        val speech = FakeSpeechRecognitionRepository()
        val viewModel = createViewModel(
            repository = FakeNotesRepository(),
            speechRecognitionRepository = speech,
        )
        runCurrent()
        viewModel.onBodyChanged("Первый абзац")

        viewModel.startVoiceInput()
        advanceTimeBy(2_000)
        runCurrent()

        val recording = (viewModel.state.value as NoteEditorUiState.Content).voiceInput
            as NoteVoiceInputUiState.Recording
        assertEquals(2, recording.durationSeconds)
        assertEquals(listOf("ru-RU"), speech.startedLocales)
        assertFalse((viewModel.state.value as NoteEditorUiState.Content).isSaveEnabled)

        speech.emit(SpeechRecognitionEvent.Result(" продолжение заметки "))
        runCurrent()

        val content = viewModel.state.value as NoteEditorUiState.Content
        assertEquals("Первый абзац продолжение заметки", content.body)
        assertEquals(NoteVoiceInputUiState.Idle, content.voiceInput)
        assertTrue(content.isSaveEnabled)
    }

    @Test
    fun `stop switches voice input to processing and delegates once`() = runTest {
        val speech = FakeSpeechRecognitionRepository()
        val viewModel = createViewModel(
            repository = FakeNotesRepository(),
            speechRecognitionRepository = speech,
        )
        runCurrent()

        viewModel.startVoiceInput()
        viewModel.stopVoiceInput()
        viewModel.stopVoiceInput()

        assertEquals(1, speech.stopCount)
        assertEquals(
            NoteVoiceInputUiState.Processing,
            (viewModel.state.value as NoteEditorUiState.Content).voiceInput,
        )
    }

    @Test
    fun `speech error preserves body and can be retried`() = runTest {
        val speech = FakeSpeechRecognitionRepository()
        val viewModel = createViewModel(
            repository = FakeNotesRepository(),
            speechRecognitionRepository = speech,
        )
        runCurrent()
        viewModel.onBodyChanged("Сохранённый текст")
        viewModel.startVoiceInput()

        speech.emit(SpeechRecognitionEvent.Error(SpeechRecognitionFailure.Network))
        runCurrent()

        val failed = viewModel.state.value as NoteEditorUiState.Content
        assertEquals("Сохранённый текст", failed.body)
        assertEquals(
            NoteVoiceInputUiState.Error(SpeechRecognitionFailure.Network),
            failed.voiceInput,
        )

        viewModel.startVoiceInput()

        assertEquals(2, speech.startedLocales.size)
        assertTrue(
            (viewModel.state.value as NoteEditorUiState.Content).voiceInput
                is NoteVoiceInputUiState.Recording,
        )
        viewModel.cancelVoiceInput()
    }

    @Test
    fun `cancel ignores a stale recognition result`() = runTest {
        val speech = FakeSpeechRecognitionRepository()
        val viewModel = createViewModel(
            repository = FakeNotesRepository(),
            speechRecognitionRepository = speech,
        )
        runCurrent()
        val cancelCountBeforeRecording = speech.cancelCount
        viewModel.onBodyChanged("Исходный текст")
        viewModel.startVoiceInput()

        viewModel.cancelVoiceInput()
        speech.emit(SpeechRecognitionEvent.Result("не добавлять"))
        runCurrent()

        val content = viewModel.state.value as NoteEditorUiState.Content
        assertEquals("Исходный текст", content.body)
        assertEquals(NoteVoiceInputUiState.Idle, content.voiceInput)
        assertEquals(cancelCountBeforeRecording + 1, speech.cancelCount)
    }

    @Test
    fun `microphone denial is represented without starting recognition`() = runTest {
        val speech = FakeSpeechRecognitionRepository()
        val viewModel = createViewModel(
            repository = FakeNotesRepository(),
            speechRecognitionRepository = speech,
        )
        runCurrent()

        viewModel.onMicrophonePermissionDenied(canRequestAgain = false)

        assertEquals(
            NoteVoiceInputUiState.PermissionDenied(canRequestAgain = false),
            (viewModel.state.value as NoteEditorUiState.Content).voiceInput,
        )
        assertTrue(speech.startedLocales.isEmpty())
    }

    private fun createViewModel(
        repository: FakeNotesRepository,
        noteId: Long? = null,
        imageStorage: FakeNoteImageStorage = FakeNoteImageStorage(),
        speechRecognitionRepository: FakeSpeechRecognitionRepository =
            FakeSpeechRecognitionRepository(),
    ) = NoteEditorViewModel(
        args = NoteEditorArgs(noteId),
        notesRepository = repository,
        noteTitleGenerator = titleGenerator,
        imageStorage = imageStorage,
        speechRecognitionRepository = speechRecognitionRepository,
    )
}
