package dev.toothlonely.notesapp.feature.notes.impl.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue

class NotesDestinationState {
    private var deactivationAction: (() -> Unit)? = null
    var handledNotesRevision by mutableLongStateOf(0L)
        private set

    fun deactivate() {
        deactivationAction?.invoke()
    }

    internal fun markNotesRevisionHandled(revision: Long) {
        handledNotesRevision = revision
    }

    internal fun attachDeactivationAction(action: () -> Unit): () -> Unit {
        deactivationAction = action
        return {
            if (deactivationAction === action) {
                deactivationAction = null
            }
        }
    }
}
