package dev.toothlonely.notesapp.feature.notes.impl.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.toothlonely.notesapp.feature.notes.impl.domain.NotesViewMode
import dev.toothlonely.notesapp.feature.notes.impl.domain.NotesViewModeRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class DataStoreNotesViewModeRepository(
    private val dataStore: DataStore<Preferences>,
) : NotesViewModeRepository {
    override fun observeViewMode(): Flow<NotesViewMode> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences -> preferences[VIEW_MODE_KEY].toViewMode() }
        .distinctUntilChanged()

    override suspend fun setViewMode(viewMode: NotesViewMode) {
        dataStore.edit { preferences ->
            preferences[VIEW_MODE_KEY] = viewMode.storageValue
        }
    }

    private fun String?.toViewMode(): NotesViewMode = when (this) {
        GRID_VALUE -> NotesViewMode.Grid
        else -> NotesViewMode.List
    }

    private val NotesViewMode.storageValue: String
        get() = when (this) {
            NotesViewMode.List -> LIST_VALUE
            NotesViewMode.Grid -> GRID_VALUE
        }

    private companion object {
        val VIEW_MODE_KEY = stringPreferencesKey("notes_view_mode")
        const val LIST_VALUE = "list"
        const val GRID_VALUE = "grid"
    }
}
