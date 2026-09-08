package dev.toothlonely.notesapp.feature.notes.impl.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.shape.RoundedCornerShape
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppShapes
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppMotion

/** A stable container identity shared by the list/grid card and every editor loading state. */
@Composable
internal fun Modifier.noteContainerTransition(
    noteId: Long?,
    isEditor: Boolean = false,
): Modifier {
    val scope = LocalNoteTransitionScope.current
    if (scope == null || noteId == null) return this
    val visibilityScope = LocalNavAnimatedContentScope.current
    val expansion by visibilityScope.transition.animateFloat(
        transitionSpec = { tween(NotesAppMotion.containerTransformMillis) },
        label = "noteContainerExpansion",
    ) { state ->
        if ((state == EnterExitState.Visible) == isEditor) 1f else 0f
    }
    return with(scope) {
        sharedBounds(
            sharedContentState = rememberSharedContentState(key = "note-container-$noteId"),
            animatedVisibilityScope = visibilityScope,
            boundsTransform = { _, _ -> tween(NotesAppMotion.containerTransformMillis) },
            enter = fadeIn(tween(NotesAppMotion.containerTransformMillis)),
            exit = fadeOut(tween(NotesAppMotion.containerTransformMillis)),
            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
            clipInOverlayDuringTransition = OverlayClip(
                RoundedCornerShape(NotesAppShapes.extraLargeCornerRadius * (1f - expansion)),
            ),
        )
    }
}
