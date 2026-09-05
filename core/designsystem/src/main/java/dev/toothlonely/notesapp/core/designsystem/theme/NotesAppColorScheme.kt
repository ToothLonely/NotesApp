package dev.toothlonely.notesapp.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

internal fun notesAppColorScheme(
    accentPalette: NotesAppAccentPalette,
    useDarkColors: Boolean,
): ColorScheme {
    val accent = accentPalette.colors
    return if (useDarkColors) {
        darkColorScheme(
            primary = accent.darkPrimary,
            onPrimary = accent.darkOnPrimary,
            primaryContainer = accent.darkPrimaryContainer,
            onPrimaryContainer = accent.darkOnPrimaryContainer,
            inversePrimary = accent.lightPrimary,
            secondary = DarkSecondary,
            onSecondary = DarkOnSecondary,
            secondaryContainer = DarkSecondaryContainer,
            onSecondaryContainer = DarkOnSecondaryContainer,
            tertiary = DarkTertiary,
            onTertiary = DarkOnTertiary,
            tertiaryContainer = DarkTertiaryContainer,
            onTertiaryContainer = DarkOnTertiaryContainer,
            background = DarkBackground,
            onBackground = DarkOnSurface,
            surface = DarkBackground,
            onSurface = DarkOnSurface,
            surfaceVariant = DarkSurfaceContainerHighest,
            onSurfaceVariant = DarkOnSurfaceVariant,
            surfaceTint = Color.Transparent,
            inverseSurface = DarkInverseSurface,
            inverseOnSurface = DarkInverseOnSurface,
            error = DarkError,
            onError = DarkOnError,
            errorContainer = DarkErrorContainer,
            onErrorContainer = DarkOnErrorContainer,
            outline = DarkOutline,
            outlineVariant = DarkOutlineVariant,
            scrim = Scrim,
            surfaceBright = DarkSurfaceBright,
            surfaceDim = DarkSurfaceDim,
            surfaceContainer = DarkSurfaceContainer,
            surfaceContainerHigh = DarkSurfaceContainerHigh,
            surfaceContainerHighest = DarkSurfaceContainerHighest,
            surfaceContainerLow = DarkSurfaceContainerLow,
            surfaceContainerLowest = DarkSurfaceContainerLowest,
        )
    } else {
        lightColorScheme(
            primary = accent.lightPrimary,
            onPrimary = accent.lightOnPrimary,
            primaryContainer = accent.lightPrimaryContainer,
            onPrimaryContainer = accent.lightOnPrimaryContainer,
            inversePrimary = accent.darkPrimary,
            secondary = LightSecondary,
            onSecondary = LightOnSecondary,
            secondaryContainer = LightSecondaryContainer,
            onSecondaryContainer = LightOnSecondaryContainer,
            tertiary = LightTertiary,
            onTertiary = LightOnTertiary,
            tertiaryContainer = LightTertiaryContainer,
            onTertiaryContainer = LightOnTertiaryContainer,
            background = LightBackground,
            onBackground = LightOnSurface,
            surface = LightBackground,
            onSurface = LightOnSurface,
            surfaceVariant = LightSurfaceContainerHighest,
            onSurfaceVariant = LightOnSurfaceVariant,
            surfaceTint = Color.Transparent,
            inverseSurface = LightInverseSurface,
            inverseOnSurface = LightInverseOnSurface,
            error = LightError,
            onError = LightOnError,
            errorContainer = LightErrorContainer,
            onErrorContainer = LightOnErrorContainer,
            outline = LightOutline,
            outlineVariant = LightOutlineVariant,
            scrim = Scrim,
            surfaceBright = LightSurfaceBright,
            surfaceDim = LightSurfaceDim,
            surfaceContainer = LightSurfaceContainer,
            surfaceContainerHigh = LightSurfaceContainerHigh,
            surfaceContainerHighest = LightSurfaceContainerHighest,
            surfaceContainerLow = LightSurfaceContainerLow,
            surfaceContainerLowest = LightSurfaceContainerLowest,
        )
    }
}
