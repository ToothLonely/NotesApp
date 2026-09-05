package dev.toothlonely.notesapp.core.designsystem.theme

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotesAppThemeModeTest {
    @Test
    fun systemMode_followsSystemSetting() {
        assertFalse(NotesAppThemeMode.System.shouldUseDarkColors(systemInDarkTheme = false))
        assertTrue(NotesAppThemeMode.System.shouldUseDarkColors(systemInDarkTheme = true))
    }

    @Test
    fun lightMode_alwaysUsesLightColors() {
        assertFalse(NotesAppThemeMode.Light.shouldUseDarkColors(systemInDarkTheme = false))
        assertFalse(NotesAppThemeMode.Light.shouldUseDarkColors(systemInDarkTheme = true))
    }

    @Test
    fun darkMode_alwaysUsesDarkColors() {
        assertTrue(NotesAppThemeMode.Dark.shouldUseDarkColors(systemInDarkTheme = false))
        assertTrue(NotesAppThemeMode.Dark.shouldUseDarkColors(systemInDarkTheme = true))
    }
}
