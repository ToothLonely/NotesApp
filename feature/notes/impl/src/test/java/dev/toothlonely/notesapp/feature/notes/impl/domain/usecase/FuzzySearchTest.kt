package dev.toothlonely.notesapp.feature.notes.impl.domain.usecase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FuzzySearchTest {
    @Test
    fun `query matches candidate when its characters stay in order`() {
        assertTrue(isFuzzySubsequence(queryWord = "хута", candidateWord = "хуета"))
    }

    @Test
    fun `query does not match when candidate ends first or order changes`() {
        assertFalse(isFuzzySubsequence(queryWord = "хуета", candidateWord = "хута"))
        assertFalse(isFuzzySubsequence(queryWord = "хтае", candidateWord = "хуета"))
    }
}
