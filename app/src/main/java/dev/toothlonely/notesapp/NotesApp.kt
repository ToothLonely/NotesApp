package dev.toothlonely.notesapp

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.notes.api.NoteEditorRoute
import dev.toothlonely.notesapp.feature.notes.api.NotesRoute
import dev.toothlonely.notesapp.feature.notes.impl.navigation.notesEntries

@Composable
fun NotesApp() {
    NotesAppTheme {
        val backStack = rememberNavBackStack(NotesRoute)

        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider<NavKey> {
                notesEntries(
                    onCreateNote = { backStack.add(NoteEditorRoute()) },
                    onCloseEditor = { backStack.removeLastOrNull() },
                )
            },
        )
    }
}
