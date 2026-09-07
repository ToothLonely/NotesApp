package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes

import dev.toothlonely.notesapp.feature.notes.impl.testutil.FakeNotesRepository
import dev.toothlonely.notesapp.feature.notes.impl.testutil.FakeNotesViewModeRepository
import dev.toothlonely.notesapp.feature.notes.impl.testutil.MainDispatcherRule
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.Note
import dev.toothlonely.notesapp.feature.notes.impl.domain.usecase.NoteListProcessor
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NoteUpdate
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NotesSortOrder
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NotesViewMode
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
        val viewModel = createViewModel(FakeNotesRepository())

        assertEquals(NotesUiState(), viewModel.state.value)
    }

    @Test
    fun `initialization requests orphan image cleanup`() = runTest {
        val repository = FakeNotesRepository()

        createViewModel(repository)
        runCurrent()

        assertEquals(1, repository.cleanupCount)
    }

    @Test
    fun `notes flow moves from loading to empty and content`() = runTest {
        val repository = FakeNotesRepository()
        val viewModel = createViewModel(repository)
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
        val viewModel = createViewModel(repository)
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
        val viewModel = createViewModel(repository)
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
        val viewModel = createViewModel(FakeNotesRepository())
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
        val viewModel = createViewModel(repository)
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
        val viewModel = createViewModel(repository)
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

    @Test
    fun `draft query does not filter until search and clear restores all notes`() = runTest {
        val repository = FakeNotesRepository().apply {
            notes.value = listOf(
                note(id = 1, title = "Работа"),
                note(id = 2, title = "Покупки"),
            )
        }
        val viewModel = createViewModel(repository)
        collectState(viewModel)
        runCurrent()

        viewModel.updateDraftQuery("покуп")
        runCurrent()

        assertEquals(
            listOf(2L, 1L),
            (viewModel.state.value.content as NotesContentState.Content).notes.map(Note::id),
        )
        assertEquals("", viewModel.state.value.appliedQuery)

        viewModel.applySearch()
        runCurrent()

        assertEquals(
            listOf(2L),
            (viewModel.state.value.content as NotesContentState.Content).notes.map(Note::id),
        )
        assertEquals("покуп", viewModel.state.value.appliedQuery)

        viewModel.updateDraftQuery("другой черновик")
        runCurrent()

        assertEquals(
            listOf(2L),
            (viewModel.state.value.content as NotesContentState.Content).notes.map(Note::id),
        )

        viewModel.clearSearch()
        runCurrent()

        assertEquals(
            listOf(2L, 1L),
            (viewModel.state.value.content as NotesContentState.Content).notes.map(Note::id),
        )
        assertEquals("", viewModel.state.value.draftQuery)
        assertEquals("", viewModel.state.value.appliedQuery)
    }

    @Test
    fun `confirmed unmatched query shows search empty`() = runTest {
        val repository = FakeNotesRepository().apply {
            notes.value = listOf(note(id = 1, title = "Работа"))
        }
        val viewModel = createViewModel(repository)
        collectState(viewModel)
        runCurrent()

        viewModel.updateDraftQuery("  покупки  ")
        viewModel.applySearch()
        runCurrent()

        assertEquals(
            NotesContentState.SearchEmpty,
            viewModel.state.value.content,
        )
        assertEquals("покупки", viewModel.state.value.appliedQuery)
    }

    @Test
    fun `fuzzy title match is shown before a newer fuzzy content match`() = runTest {
        val repository = FakeNotesRepository().apply {
            notes.value = listOf(
                note(id = 1, title = "Хуета", updatedAtMillis = 100),
                note(
                    id = 2,
                    title = "Заметка 2",
                    content = "Текст содержит слово хуета",
                    updatedAtMillis = 200,
                ),
            )
        }
        val viewModel = createViewModel(repository)
        collectState(viewModel)
        runCurrent()

        viewModel.updateDraftQuery("хута")
        viewModel.applySearch()
        runCurrent()

        assertEquals(
            listOf(1L, 2L),
            (viewModel.state.value.content as NotesContentState.Content).notes.map(Note::id),
        )
    }

    @Test
    fun `sort order applies to full list and search with stable id tie breaker`() = runTest {
        val repository = FakeNotesRepository().apply {
            notes.value = listOf(
                note(id = 1, title = "Матч один", updatedAtMillis = 100),
                note(id = 2, title = "Другое", updatedAtMillis = 100),
                note(id = 3, title = "Матч два", updatedAtMillis = 200),
            )
        }
        val viewModel = createViewModel(repository)
        collectState(viewModel)
        runCurrent()

        assertEquals(
            listOf(3L, 2L, 1L),
            (viewModel.state.value.content as NotesContentState.Content).notes.map(Note::id),
        )

        viewModel.changeSortOrder(NotesSortOrder.OldestFirst)
        runCurrent()

        assertEquals(
            listOf(1L, 2L, 3L),
            (viewModel.state.value.content as NotesContentState.Content).notes.map(Note::id),
        )

        viewModel.updateDraftQuery("матч")
        viewModel.applySearch()
        runCurrent()

        assertEquals(
            listOf(1L, 3L),
            (viewModel.state.value.content as NotesContentState.Content).notes.map(Note::id),
        )
    }

    @Test
    fun `view mode is loaded and successful change is persisted`() = runTest {
        val viewModeRepository = FakeNotesViewModeRepository(NotesViewMode.Grid)
        val viewModel = createViewModel(
            repository = FakeNotesRepository(),
            viewModeRepository = viewModeRepository,
        )
        collectState(viewModel)
        runCurrent()

        assertEquals(NotesViewMode.Grid, viewModel.state.value.viewMode)

        viewModel.changeViewMode(NotesViewMode.List)
        runCurrent()

        assertEquals(listOf(NotesViewMode.List), viewModeRepository.setRequests)
        assertEquals(NotesViewMode.List, viewModel.state.value.viewMode)
        assertFalse(viewModel.state.value.isViewModeSaving)
        assertFalse(viewModel.state.value.hasViewModeSaveError)
    }

    @Test
    fun `view mode save failure keeps mode and can be retried`() = runTest {
        val viewModeRepository = FakeNotesViewModeRepository().apply {
            setFailure = IllegalStateException("DataStore write failed")
        }
        val viewModel = createViewModel(
            repository = FakeNotesRepository(),
            viewModeRepository = viewModeRepository,
        )
        collectState(viewModel)
        runCurrent()

        viewModel.changeViewMode(NotesViewMode.Grid)
        runCurrent()

        assertEquals(NotesViewMode.List, viewModel.state.value.viewMode)
        assertTrue(viewModel.state.value.hasViewModeSaveError)

        viewModeRepository.setFailure = null
        viewModel.retryViewModeChange()
        runCurrent()

        assertEquals(
            listOf(NotesViewMode.Grid, NotesViewMode.Grid),
            viewModeRepository.setRequests,
        )
        assertEquals(NotesViewMode.Grid, viewModel.state.value.viewMode)
        assertFalse(viewModel.state.value.hasViewModeSaveError)
    }

    @Test
    fun `query sort grid and delete mode share one visible note set`() = runTest {
        val repository = FakeNotesRepository().apply {
            notes.value = listOf(
                note(id = 1, title = "Проект альфа", updatedAtMillis = 300),
                note(id = 2, title = "Проект бета", updatedAtMillis = 100),
                note(id = 3, title = "Личное", updatedAtMillis = 200),
            )
        }
        val viewModel = createViewModel(
            repository = repository,
            viewModeRepository = FakeNotesViewModeRepository(NotesViewMode.Grid),
        )
        collectState(viewModel)
        runCurrent()

        viewModel.updateDraftQuery("проект")
        viewModel.applySearch()
        viewModel.changeSortOrder(NotesSortOrder.OldestFirst)
        viewModel.toggleDeleteMode()
        runCurrent()

        assertEquals(NotesViewMode.Grid, viewModel.state.value.viewMode)
        assertEquals(NotesSortOrder.OldestFirst, viewModel.state.value.sortOrder)
        assertTrue(viewModel.state.value.isDeleteMode)
        assertEquals(
            listOf(2L, 1L),
            (viewModel.state.value.content as NotesContentState.Content).notes.map(Note::id),
        )
    }

    private fun kotlinx.coroutines.test.TestScope.collectState(viewModel: NotesViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect()
        }
    }

    private fun note(
        id: Long,
        title: String = "Заметка $id",
        content: String = "Текст",
        updatedAtMillis: Long = id,
    ) = Note(
        id = id,
        title = title,
        content = content,
        createdAtMillis = id,
        updatedAtMillis = updatedAtMillis,
    )

    private fun createViewModel(
        repository: FakeNotesRepository,
        viewModeRepository: FakeNotesViewModeRepository = FakeNotesViewModeRepository(),
    ) = NotesViewModel(
        notesRepository = repository,
        notesViewModeRepository = viewModeRepository,
        noteListProcessor = NoteListProcessor(),
        searchDispatcher = mainDispatcherRule.testDispatcher,
    )
}
