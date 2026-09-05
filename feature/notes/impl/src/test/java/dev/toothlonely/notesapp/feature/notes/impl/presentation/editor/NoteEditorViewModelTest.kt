package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor

import dev.toothlonely.notesapp.feature.notes.impl.FakeNotesRepository
import dev.toothlonely.notesapp.feature.notes.impl.MainDispatcherRule
import dev.toothlonely.notesapp.feature.notes.impl.domain.NewNote
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
        val viewModel = NoteEditorViewModel(FakeNotesRepository(), titleGenerator)

        assertEquals(NoteEditorUiState.Loading, viewModel.state.value)
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
        val viewModel = NoteEditorViewModel(FakeNotesRepository(), titleGenerator)
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
        val viewModel = NoteEditorViewModel(repository, titleGenerator)
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
        val viewModel = NoteEditorViewModel(repository, titleGenerator)
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
        val viewModel = NoteEditorViewModel(repository, titleGenerator)
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
        val viewModel = NoteEditorViewModel(repository, titleGenerator)
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
        val viewModel = NoteEditorViewModel(repository, titleGenerator)
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
        val viewModel = NoteEditorViewModel(repository, titleGenerator)
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
}
