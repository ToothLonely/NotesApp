package dev.toothlonely.notesapp.feature.notes.impl

import dev.toothlonely.notesapp.feature.notes.impl.domain.NotesViewMode
import dev.toothlonely.notesapp.feature.notes.impl.domain.NotesViewModeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeNotesViewModeRepository(
    initialViewMode: NotesViewMode = NotesViewMode.List,
) : NotesViewModeRepository {
    val viewMode = MutableStateFlow(initialViewMode)
    val setRequests = mutableListOf<NotesViewMode>()
    var setFailure: Throwable? = null

    override fun observeViewMode(): Flow<NotesViewMode> = viewMode

    override suspend fun setViewMode(viewMode: NotesViewMode) {
        setRequests += viewMode
        setFailure?.let { throw it }
        this.viewMode.value = viewMode
    }
}
