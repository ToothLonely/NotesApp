package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes

import dev.toothlonely.notesapp.feature.notes.impl.FakeNotesRepository
import dev.toothlonely.notesapp.feature.notes.impl.MainDispatcherRule
import dev.toothlonely.notesapp.feature.notes.impl.domain.Note
import dev.toothlonely.notesapp.feature.notes.impl.domain.NoteUpdate
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state is loading`() = runTest {
        val viewModel = NotesViewModel(FakeNotesRepository())

        assertEquals(NotesUiState(), viewModel.state.value)
    }

    @Test
    fun `notes flow moves from loading to empty and content`() = runTest {
        val repository = FakeNotesRepository()
        val viewModel = NotesViewModel(repository)
        collectState(viewModel)

        runCurrent()
        assertEquals(NotesContentState.Empty, viewModel.state.value.content)

        val note = note(id = 1)
        repository.notes.value = listOf(note)
        runCurrent()

        assertEquals(NotesContentState.Content(listOf(note)), viewModel.state.value.content)
    }

    @Test
    fun `most recently updated note moves to the start of the list`() = runTest {
        val repository = FakeNotesRepository().apply {
            notes.value = listOf(
                note(id = 1, updatedAtMillis = 100),
                note(id = 2, updatedAtMillis = 200),
            )
            currentTimeMillis = 300
        }
        val viewModel = NotesViewModel(repository)
        collectState(viewModel)
        runCurrent()

        assertEquals(
            listOf(2L, 1L),
            (viewModel.state.value.content as NotesContentState.Content).notes.map(Note::id),
        )

        repository.updateNote(
            NoteUpdate(
                id = 1,
                title = "Изменённая заметка",
                content = "Новый текст",
                generatedTitleNumber = null,
            ),
        )
        runCurrent()

        assertEquals(
            listOf(1L, 2L),
            (viewModel.state.value.content as NotesContentState.Content).notes.map(Note::id),
        )
    }

    @Test
    fun `flow failure shows error and retry subscribes again`() = runTest {
        val repository = FakeNotesRepository().apply {
            observeFailure = IllegalStateException("Database unavailable")
        }
        val viewModel = NotesViewModel(repository)
        collectState(viewModel)

        runCurrent()
        assertEquals(NotesContentState.Error, viewModel.state.value.content)

        repository.observeFailure = null
        viewModel.retryLoading()
        runCurrent()

        assertEquals(NotesContentState.Empty, viewModel.state.value.content)
    }

    @Test
    fun `delete mode can be enabled and disabled`() = runTest {
        val viewModel = NotesViewModel(FakeNotesRepository())
        collectState(viewModel)

        viewModel.toggleDeleteMode()
        runCurrent()
        assertTrue(viewModel.state.value.isDeleteMode)

        viewModel.toggleDeleteMode()
        runCurrent()
        assertFalse(viewModel.state.value.isDeleteMode)
    }

    @Test
    fun `note remains visible until delete succeeds then last note moves list to empty`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repository = FakeNotesRepository().apply {
            notes.value = listOf(note(id = 9))
            deleteGate = gate
        }
        val viewModel = NotesViewModel(repository)
        collectState(viewModel)
        runCurrent()
        viewModel.toggleDeleteMode()
        val event = async { viewModel.events.first() }

        viewModel.deleteNote(9)
        runCurrent()

        assertEquals(
            NotesContentState.Content(repository.notes.value),
            viewModel.state.value.content,
        )
        assertEquals(setOf(9L), viewModel.state.value.deletingNoteIds)

        gate.complete(Unit)
        runCurrent()

        assertEquals(NotesEvent.NoteDeleted, event.await())
        assertEquals(listOf(9L), repository.deletedNoteIds)
        assertEquals(NotesContentState.Empty, viewModel.state.value.content)
        assertTrue(viewModel.state.value.deletingNoteIds.isEmpty())
    }

    @Test
    fun `delete failure keeps note and exposes retryable error`() = runTest {
        val repository = FakeNotesRepository().apply {
            notes.value = listOf(note(id = 4))
            deleteFailure = IllegalStateException("Delete failed")
        }
        val viewModel = NotesViewModel(repository)
        collectState(viewModel)
        runCurrent()
        viewModel.toggleDeleteMode()

        viewModel.deleteNote(4)
        runCurrent()

        assertEquals(listOf(4L), repository.notes.value.map(Note::id))
        assertEquals(4L, viewModel.state.value.failedDeleteNoteId)
        assertTrue(viewModel.state.value.isDeleteMode)

        repository.deleteFailure = null
        viewModel.retryDelete()
        runCurrent()

        assertEquals(NotesContentState.Empty, viewModel.state.value.content)
        assertEquals(null, viewModel.state.value.failedDeleteNoteId)
    }

    private fun kotlinx.coroutines.test.TestScope.collectState(viewModel: NotesViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect()
        }
    }

    private fun note(
        id: Long,
        updatedAtMillis: Long = id,
    ) = Note(
        id = id,
        title = "Заметка $id",
        content = "Текст",
        createdAtMillis = id,
        updatedAtMillis = updatedAtMillis,
    )
}
