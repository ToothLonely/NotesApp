package dev.toothlonely.notesapp.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

enum class NotesAppThemeMode {
    System,
    Light,
    Dark,
    ;

    internal fun shouldUseDarkColors(systemInDarkTheme: Boolean): Boolean = when (this) {
        System -> systemInDarkTheme
        Light -> false
        Dark -> true
    }
}

enum class NotesAppAccentPalette {
    Indigo,
    Teal,
    Raspberry,
    Amber,
}

@Composable
fun NotesAppTheme(
    themeMode: NotesAppThemeMode = NotesAppThemeMode.System,
    accentPalette: NotesAppAccentPalette = NotesAppAccentPalette.Indigo,
    content: @Composable () -> Unit,
) {
    val useDarkColors = themeMode.shouldUseDarkColors(isSystemInDarkTheme())
    val colorScheme = remember(accentPalette, useDarkColors) {
        notesAppColorScheme(
            accentPalette = accentPalette,
            useDarkColors = useDarkColors,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NotesAppTypography,
        shapes = NotesAppMaterialShapes,
        content = content,
    )
}
