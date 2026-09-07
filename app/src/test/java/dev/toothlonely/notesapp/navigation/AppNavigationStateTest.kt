package dev.toothlonely.notesapp.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dev.toothlonely.notesapp.feature.notes.api.NoteEditorRoute
import dev.toothlonely.notesapp.feature.notes.api.NotesRoute
import dev.toothlonely.notesapp.feature.settings.api.SettingsRoute
import dev.toothlonely.notesapp.feature.tasks.api.TasksRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNavigationStateTest {
    @Test
    fun `app starts on notes with exactly three top level destinations`() {
        val state = createNavigationState()

        assertEquals(
            listOf(NotesRoute, TasksRoute, SettingsRoute),
            AppDestination.entries.map(AppDestination::route),
        )
        assertEquals(AppDestination.Notes, state.selectedDestination)
        assertEquals(NotesRoute, state.currentRoute)
        assertEquals(listOf(NotesRoute), state.stacksInUse)
        assertTrue(state.shouldShowBottomNavigation)
    }

    @Test
    fun `switching destinations preserves each back stack`() {
        val state = createNavigationState()
        val editorRoute = NoteEditorRoute(noteId = 42L)
        state.navigate(editorRoute)

        state.selectDestination(AppDestination.Tasks)

        assertEquals(TasksRoute, state.currentRoute)
        assertEquals(listOf(TasksRoute), state.stacksInUse)
        assertEquals(listOf(NotesRoute, editorRoute), state.backStacks.getValue(NotesRoute))

        state.selectDestination(AppDestination.Notes)

        assertEquals(editorRoute, state.currentRoute)
        assertFalse(state.shouldShowBottomNavigation)
    }

    @Test
    fun `selecting current destination pops its stack to root`() {
        val state = createNavigationState()
        state.navigate(NoteEditorRoute())

        state.selectDestination(AppDestination.Notes)

        assertEquals(listOf(NotesRoute), state.currentBackStack)
        assertTrue(state.shouldShowBottomNavigation)
    }

    @Test
    fun `back pops nested route before changing top level destination`() {
        val state = createNavigationState()
        state.navigate(NoteEditorRoute(noteId = 7L))

        state.goBack()

        assertEquals(AppDestination.Notes, state.selectedDestination)
        assertEquals(NotesRoute, state.currentRoute)
    }

    @Test
    fun `back from secondary root does not navigate to notes`() {
        val state = createNavigationState()
        state.selectDestination(AppDestination.Settings)

        state.goBack()

        assertEquals(AppDestination.Settings, state.selectedDestination)
        assertEquals(listOf(SettingsRoute), state.stacksInUse)
        assertEquals(listOf(SettingsRoute), state.backStacks.getValue(SettingsRoute))
    }

    @Test
    fun `switching destination reports the destination being deactivated`() {
        val state = createNavigationState()
        var deactivatedDestination: AppDestination? = null

        state.selectDestination(AppDestination.Tasks) { destination ->
            deactivatedDestination = destination
        }

        assertEquals(AppDestination.Notes, deactivatedDestination)
        assertEquals(AppDestination.Tasks, state.selectedDestination)
    }

    @Test
    fun `reselecting destination does not report deactivation`() {
        val state = createNavigationState()
        var deactivationCount = 0

        state.selectDestination(AppDestination.Notes) {
            deactivationCount += 1
        }

        assertEquals(0, deactivationCount)
    }

    private fun createNavigationState(): AppNavigationState {
        val backStacks = mapOf<NavKey, NavBackStack<NavKey>>(
            NotesRoute to NavBackStack(NotesRoute),
            TasksRoute to NavBackStack(TasksRoute),
            SettingsRoute to NavBackStack(SettingsRoute),
        )
        return AppNavigationState(
            startRoute = NotesRoute,
            selectedRoute = mutableStateOf(NotesRoute),
            backStacks = backStacks,
        )
    }
}
