package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes

import dev.toothlonely.notesapp.feature.notes.impl.FakeNotesRepository
import dev.toothlonely.notesapp.feature.notes.impl.MainDispatcherRule
import dev.toothlonely.notesapp.feature.notes.impl.domain.Note
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state is loading`() = runTest {
        val viewModel = NotesViewModel(FakeNotesRepository())

        assertEquals(NotesUiState.Loading, viewModel.state.value)
    }

    @Test
    fun `notes flow moves from loading to empty and content`() = runTest {
        val repository = FakeNotesRepository()
        val viewModel = NotesViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect()
        }

        runCurrent()
        assertEquals(NotesUiState.Empty, viewModel.state.value)

        val note = Note(id = 1, title = "Заметка", content = "Текст", createdAtMillis = 10)
        repository.notes.value = listOf(note)
        runCurrent()

        assertEquals(NotesUiState.Content(listOf(note)), viewModel.state.value)
    }

    @Test
    fun `flow failure shows error and retry subscribes again`() = runTest {
        val repository = FakeNotesRepository().apply {
            observeFailure = IllegalStateException("Database unavailable")
        }
        val viewModel = NotesViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect()
        }

        runCurrent()
        assertEquals(NotesUiState.Error, viewModel.state.value)

        repository.observeFailure = null
        viewModel.retry()
        runCurrent()

        assertEquals(NotesUiState.Empty, viewModel.state.value)
    }
}
