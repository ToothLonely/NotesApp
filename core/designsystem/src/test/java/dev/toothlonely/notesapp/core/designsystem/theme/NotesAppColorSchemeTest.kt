package dev.toothlonely.notesapp.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class NotesAppColorSchemeTest {
    @Test
    fun accentPalettes_applyApprovedPrimaryRoles() {
        val expectedColors = mapOf(
            NotesAppAccentPalette.Indigo to ExpectedAccentColors(
                lightPrimary = Color(0xFF4F56A6),
                lightOnPrimary = Color(0xFFFFFFFF),
                lightPrimaryContainer = Color(0xFFE0E0FF),
                lightOnPrimaryContainer = Color(0xFF090E5D),
                darkPrimary = Color(0xFFBEC2FF),
                darkOnPrimary = Color(0xFF1E276E),
                darkPrimaryContainer = Color(0xFF373E86),
                darkOnPrimaryContainer = Color(0xFFE0E0FF),
            ),
            NotesAppAccentPalette.Teal to ExpectedAccentColors(
                lightPrimary = Color(0xFF006A66),
                lightOnPrimary = Color(0xFFFFFFFF),
                lightPrimaryContainer = Color(0xFF9CF2EB),
                lightOnPrimaryContainer = Color(0xFF00201E),
                darkPrimary = Color(0xFF80D5CF),
                darkOnPrimary = Color(0xFF003734),
                darkPrimaryContainer = Color(0xFF00504C),
                darkOnPrimaryContainer = Color(0xFF9CF2EB),
            ),
            NotesAppAccentPalette.Raspberry to ExpectedAccentColors(
                lightPrimary = Color(0xFF984061),
                lightOnPrimary = Color(0xFFFFFFFF),
                lightPrimaryContainer = Color(0xFFFFD9E3),
                lightOnPrimaryContainer = Color(0xFF3E001D),
                darkPrimary = Color(0xFFFFB0C8),
                darkOnPrimary = Color(0xFF5E1133),
                darkPrimaryContainer = Color(0xFF7A2949),
                darkOnPrimaryContainer = Color(0xFFFFD9E3),
            ),
            NotesAppAccentPalette.Amber to ExpectedAccentColors(
                lightPrimary = Color(0xFF765A00),
                lightOnPrimary = Color(0xFFFFFFFF),
                lightPrimaryContainer = Color(0xFFFFE080),
                lightOnPrimaryContainer = Color(0xFF241A00),
                darkPrimary = Color(0xFFEAC300),
                darkOnPrimary = Color(0xFF3D2F00),
                darkPrimaryContainer = Color(0xFF594400),
                darkOnPrimaryContainer = Color(0xFFFFE080),
            ),
        )

        expectedColors.forEach { (palette, expected) ->
            val lightScheme = notesAppColorScheme(palette, useDarkColors = false)
            val darkScheme = notesAppColorScheme(palette, useDarkColors = true)

            assertEquals(expected.lightPrimary, lightScheme.primary)
            assertEquals(expected.lightOnPrimary, lightScheme.onPrimary)
            assertEquals(expected.lightPrimaryContainer, lightScheme.primaryContainer)
            assertEquals(expected.lightOnPrimaryContainer, lightScheme.onPrimaryContainer)
            assertEquals(expected.darkPrimary, darkScheme.primary)
            assertEquals(expected.darkOnPrimary, darkScheme.onPrimary)
            assertEquals(expected.darkPrimaryContainer, darkScheme.primaryContainer)
            assertEquals(expected.darkOnPrimaryContainer, darkScheme.onPrimaryContainer)
        }
    }

    @Test
    fun accentPalettes_doNotChangeNeutralOrSemanticRoles() {
        val lightSchemes = NotesAppAccentPalette.entries.map {
            notesAppColorScheme(it, useDarkColors = false)
        }
        val darkSchemes = NotesAppAccentPalette.entries.map {
            notesAppColorScheme(it, useDarkColors = true)
        }

        lightSchemes.drop(1).forEach { scheme ->
            assertEquals(lightSchemes.first().surface, scheme.surface)
            assertEquals(lightSchemes.first().surfaceContainer, scheme.surfaceContainer)
            assertEquals(lightSchemes.first().error, scheme.error)
            assertEquals(lightSchemes.first().secondary, scheme.secondary)
        }
        darkSchemes.drop(1).forEach { scheme ->
            assertEquals(darkSchemes.first().surface, scheme.surface)
            assertEquals(darkSchemes.first().surfaceContainer, scheme.surfaceContainer)
            assertEquals(darkSchemes.first().error, scheme.error)
            assertEquals(darkSchemes.first().secondary, scheme.secondary)
        }
    }
}

private data class ExpectedAccentColors(
    val lightPrimary: Color,
    val lightOnPrimary: Color,
    val lightPrimaryContainer: Color,
    val lightOnPrimaryContainer: Color,
    val darkPrimary: Color,
    val darkOnPrimary: Color,
    val darkPrimaryContainer: Color,
    val darkOnPrimaryContainer: Color,
)
