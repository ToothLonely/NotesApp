package dev.toothlonely.notesapp.core.designsystem.component

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class BottomNavigationAwareSnackbarHostTest {
    @Test
    fun `bottom navigation inset is applied when fab is absent`() {
        assertEquals(
            96.dp,
            calculateSnackbarBottomPadding(
                bottomNavigationInset = 96.dp,
                isFloatingActionButtonVisible = false,
            ),
        )
    }

    @Test
    fun `scaffold handles snackbar offset when fab is visible`() {
        assertEquals(
            0.dp,
            calculateSnackbarBottomPadding(
                bottomNavigationInset = 96.dp,
                isFloatingActionButtonVisible = true,
            ),
        )
    }
}
