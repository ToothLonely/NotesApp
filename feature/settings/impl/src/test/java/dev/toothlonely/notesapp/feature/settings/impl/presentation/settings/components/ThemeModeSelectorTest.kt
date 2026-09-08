package dev.toothlonely.notesapp.feature.settings.impl.presentation.settings.components

import dev.toothlonely.notesapp.core.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeSelectorTest {
    @Test
    fun `theme cards keep approved display order`() {
        assertEquals(
            listOf(ThemeMode.System, ThemeMode.Light, ThemeMode.Dark),
            themeModesInDisplayOrder,
        )
    }
}
