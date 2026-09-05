package dev.toothlonely.notesapp.feature.notes.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object NotesRoute : NavKey

@Serializable
data class NoteEditorRoute(
    val noteId: Long? = null,
) : NavKey
