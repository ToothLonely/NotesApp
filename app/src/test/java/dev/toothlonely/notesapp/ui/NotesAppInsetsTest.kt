package dev.toothlonely.notesapp.ui

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class NotesAppInsetsTest {
    @Test
    fun `bottom navigation clearance includes system inset and content gap`() {
        assertEquals(
            104.dp,
            calculateBottomNavigationClearance(navigationBarBottomInset = 12.dp),
        )
    }

}
