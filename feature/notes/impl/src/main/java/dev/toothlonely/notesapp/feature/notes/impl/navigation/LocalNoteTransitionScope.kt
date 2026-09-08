package dev.toothlonely.notesapp.feature.notes.impl.navigation

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.staticCompositionLocalOf

val LocalNoteTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }
