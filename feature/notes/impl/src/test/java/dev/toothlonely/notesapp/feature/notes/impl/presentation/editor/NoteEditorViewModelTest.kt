package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor

import dev.toothlonely.notesapp.feature.notes.impl.FakeNotesRepository
import dev.toothlonely.notesapp.feature.notes.impl.MainDispatcherRule
import dev.toothlonely.notesapp.feature.notes.impl.domain.NewNote
import dev.toothlonely.notesapp.feature.notes.impl.domain.Note
import dev.toothlonely.notesapp.feature.notes.impl.domain.NoteUpdate
import dev.toothlonely.notesapp.feature.notes.impl.domain.NoteTitleGenerator
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
        assertTrue(state.hasSaveError)
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
        assertFalse(state.hasSaveError)
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
        assertTrue(failedState.hasSaveError)
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

    private fun createViewModel(
        repository: FakeNotesRepository,
        noteId: Long? = null,
    ) = NoteEditorViewModel(
        args = NoteEditorArgs(noteId),
        notesRepository = repository,
        noteTitleGenerator = titleGenerator,
    )
}
