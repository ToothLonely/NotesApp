package dev.toothlonely.notesapp.feature.settings.impl.presentation.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class GigaChatBalanceFormatterTest {
    @Test
    fun `formats token count in groups of three digits`() {
        assertEquals("100 000 000", formatGigaChatTokenCount(100_000_000))
    }

    @Test
    fun `does not add a separator to a short token count`() {
        assertEquals("999", formatGigaChatTokenCount(999))
    }
}
