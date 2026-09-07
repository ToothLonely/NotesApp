package dev.toothlonely.notesapp.feature.notes.impl.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NotesViewMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DataStoreNotesViewModeRepositoryTest {
    @Test
    fun `missing and unsupported values map to list`() = runTest {
        val dataStore = FakePreferencesDataStore(emptyPreferences())
        val repository = DataStoreNotesViewModeRepository(dataStore)

        assertEquals(NotesViewMode.List, repository.observeViewMode().first())

        dataStore.updateData {
            preferencesOf(VIEW_MODE_KEY to "unsupported")
        }

        assertEquals(NotesViewMode.List, repository.observeViewMode().first())
    }

    @Test
    fun `grid value is read from preferences`() = runTest {
        val dataStore = FakePreferencesDataStore(
            preferencesOf(VIEW_MODE_KEY to GRID_VALUE),
        )
        val repository = DataStoreNotesViewModeRepository(dataStore)

        assertEquals(NotesViewMode.Grid, repository.observeViewMode().first())
    }

    @Test
    fun `setting view mode persists its storage value`() = runTest {
        val dataStore = FakePreferencesDataStore(emptyPreferences())
        val repository = DataStoreNotesViewModeRepository(dataStore)

        repository.setViewMode(NotesViewMode.Grid)

        assertEquals(GRID_VALUE, dataStore.data.first()[VIEW_MODE_KEY])
    }

    private class FakePreferencesDataStore(
        initialPreferences: Preferences,
    ) : DataStore<Preferences> {
        private val state = MutableStateFlow(initialPreferences)

        override val data: Flow<Preferences> = state

        override suspend fun updateData(
            transform: suspend (t: Preferences) -> Preferences,
        ): Preferences = transform(state.value).also { updatedPreferences ->
            state.value = updatedPreferences
        }
    }

    private companion object {
        val VIEW_MODE_KEY = stringPreferencesKey("notes_view_mode")
        const val GRID_VALUE = "grid"
    }
}
