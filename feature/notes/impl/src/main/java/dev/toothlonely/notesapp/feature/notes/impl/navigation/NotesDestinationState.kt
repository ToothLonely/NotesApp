package dev.toothlonely.notesapp.feature.notes.impl.navigation

class NotesDestinationState {
    private var deactivationAction: (() -> Unit)? = null

    fun deactivate() {
        deactivationAction?.invoke()
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
