package dev.toothlonely.notesapp.core.domain.search

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FuzzySearchTest {
    @Test
    fun `query matches exact substring case insensitively`() {
        assertTrue(FuzzySearch.matches(candidate = "Купить молоко", query = "  МОЛОК  "))
    }

    @Test
    fun `query matches candidate when characters of every word stay in order`() {
        assertTrue(FuzzySearch.matches(candidate = "Купить молоко", query = "купть млко"))
    }

    @Test
    fun `query does not match when candidate ends first or character order changes`() {
        assertFalse(FuzzySearch.matches(candidate = "карта", query = "картина"))
        assertFalse(FuzzySearch.matches(candidate = "картина", query = "кнтра"))
    }

    @Test
    fun `short query requires an exact substring`() {
        assertFalse(FuzzySearch.matches(candidate = "Кот", query = "кт"))
    }
}
