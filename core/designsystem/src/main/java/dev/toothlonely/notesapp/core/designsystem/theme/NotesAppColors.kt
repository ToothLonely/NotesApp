package dev.toothlonely.notesapp.core.designsystem.theme

import androidx.compose.ui.graphics.Color

internal val LightBackground = Color(0xFFFAF9FD)
internal val LightOnSurface = Color(0xFF1B1B1F)
internal val LightSurfaceDim = Color(0xFFDBD9DE)
internal val LightSurfaceBright = Color(0xFFFAF9FD)
internal val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
internal val LightSurfaceContainerLow = Color(0xFFF5F3F8)
internal val LightSurfaceContainer = Color(0xFFEFEDF2)
internal val LightSurfaceContainerHigh = Color(0xFFE9E7EC)
internal val LightSurfaceContainerHighest = Color(0xFFE3E1E6)
internal val LightOnSurfaceVariant = Color(0xFF46464F)
internal val LightOutline = Color(0xFF777680)
internal val LightOutlineVariant = Color(0xFFC7C5D0)
internal val LightInverseSurface = Color(0xFF303034)
internal val LightInverseOnSurface = Color(0xFFF2F0F5)

internal val DarkBackground = Color(0xFF121318)
internal val DarkOnSurface = Color(0xFFE4E1E9)
internal val DarkSurfaceDim = Color(0xFF121318)
internal val DarkSurfaceBright = Color(0xFF38393F)
internal val DarkSurfaceContainerLowest = Color(0xFF0D0E13)
internal val DarkSurfaceContainerLow = Color(0xFF1A1B20)
internal val DarkSurfaceContainer = Color(0xFF1E1F24)
internal val DarkSurfaceContainerHigh = Color(0xFF292A30)
internal val DarkSurfaceContainerHighest = Color(0xFF34353B)
internal val DarkOnSurfaceVariant = Color(0xFFC7C5D0)
internal val DarkOutline = Color(0xFF918F99)
internal val DarkOutlineVariant = Color(0xFF46464F)
internal val DarkInverseSurface = Color(0xFFE4E1E9)
internal val DarkInverseOnSurface = Color(0xFF303034)

internal val LightSecondary = Color(0xFF5D5E6E)
internal val LightOnSecondary = Color(0xFFFFFFFF)
internal val LightSecondaryContainer = Color(0xFFE2E1F3)
internal val LightOnSecondaryContainer = Color(0xFF1A1B2C)
internal val LightTertiary = Color(0xFF75556F)
internal val LightOnTertiary = Color(0xFFFFFFFF)
internal val LightTertiaryContainer = Color(0xFFFFD7F5)
internal val LightOnTertiaryContainer = Color(0xFF2C122A)

internal val DarkSecondary = Color(0xFFC6C5D8)
internal val DarkOnSecondary = Color(0xFF2F3040)
internal val DarkSecondaryContainer = Color(0xFF464757)
internal val DarkOnSecondaryContainer = Color(0xFFE2E1F3)
internal val DarkTertiary = Color(0xFFE4BADB)
internal val DarkOnTertiary = Color(0xFF43283F)
internal val DarkTertiaryContainer = Color(0xFF5B3E56)
internal val DarkOnTertiaryContainer = Color(0xFFFFD7F5)

internal val LightError = Color(0xFFBA1A1A)
internal val LightOnError = Color(0xFFFFFFFF)
internal val LightErrorContainer = Color(0xFFFFDAD6)
internal val LightOnErrorContainer = Color(0xFF410002)
internal val DarkError = Color(0xFFFFB4AB)
internal val DarkOnError = Color(0xFF690005)
internal val DarkErrorContainer = Color(0xFF93000A)
internal val DarkOnErrorContainer = Color(0xFFFFDAD6)

internal val Scrim = Color(0xFF000000)

internal data class NotesAppAccentColors(
    val lightPrimary: Color,
    val lightOnPrimary: Color,
    val lightPrimaryContainer: Color,
    val lightOnPrimaryContainer: Color,
    val darkPrimary: Color,
    val darkOnPrimary: Color,
    val darkPrimaryContainer: Color,
    val darkOnPrimaryContainer: Color,
)

internal val NotesAppAccentPalette.colors: NotesAppAccentColors
    get() = when (this) {
        NotesAppAccentPalette.Indigo -> NotesAppAccentColors(
            lightPrimary = Color(0xFF4F56A6),
            lightOnPrimary = Color(0xFFFFFFFF),
            lightPrimaryContainer = Color(0xFFE0E0FF),
            lightOnPrimaryContainer = Color(0xFF090E5D),
            darkPrimary = Color(0xFFBEC2FF),
            darkOnPrimary = Color(0xFF1E276E),
            darkPrimaryContainer = Color(0xFF373E86),
            darkOnPrimaryContainer = Color(0xFFE0E0FF),
        )
        NotesAppAccentPalette.Teal -> NotesAppAccentColors(
            lightPrimary = Color(0xFF006A66),
            lightOnPrimary = Color(0xFFFFFFFF),
            lightPrimaryContainer = Color(0xFF9CF2EB),
            lightOnPrimaryContainer = Color(0xFF00201E),
            darkPrimary = Color(0xFF80D5CF),
            darkOnPrimary = Color(0xFF003734),
            darkPrimaryContainer = Color(0xFF00504C),
            darkOnPrimaryContainer = Color(0xFF9CF2EB),
        )
        NotesAppAccentPalette.Raspberry -> NotesAppAccentColors(
            lightPrimary = Color(0xFF984061),
            lightOnPrimary = Color(0xFFFFFFFF),
            lightPrimaryContainer = Color(0xFFFFD9E3),
            lightOnPrimaryContainer = Color(0xFF3E001D),
            darkPrimary = Color(0xFFFFB0C8),
            darkOnPrimary = Color(0xFF5E1133),
            darkPrimaryContainer = Color(0xFF7A2949),
            darkOnPrimaryContainer = Color(0xFFFFD9E3),
        )
        NotesAppAccentPalette.Amber -> NotesAppAccentColors(
            lightPrimary = Color(0xFF765A00),
            lightOnPrimary = Color(0xFFFFFFFF),
            lightPrimaryContainer = Color(0xFFFFE080),
            lightOnPrimaryContainer = Color(0xFF241A00),
            darkPrimary = Color(0xFFEAC300),
            darkOnPrimary = Color(0xFF3D2F00),
            darkPrimaryContainer = Color(0xFF594400),
            darkOnPrimaryContainer = Color(0xFFFFE080),
        )
    }
