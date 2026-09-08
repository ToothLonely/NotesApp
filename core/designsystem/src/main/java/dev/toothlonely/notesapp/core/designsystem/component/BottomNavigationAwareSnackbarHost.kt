package dev.toothlonely.notesapp.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme

@Composable
fun BottomNavigationAwareSnackbarHost(
    bottomNavigationPadding: PaddingValues,
    isFloatingActionButtonVisible: Boolean,
    snackbarHost: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(
            bottom = calculateSnackbarBottomPadding(
                bottomNavigationInset = bottomNavigationPadding.calculateBottomPadding(),
                isFloatingActionButtonVisible = isFloatingActionButtonVisible,
            ),
        ),
    ) {
        snackbarHost()
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomNavigationAwareSnackbarHostPreview() {
    NotesAppTheme {
        BottomNavigationAwareSnackbarHost(
            bottomNavigationPadding = PaddingValues(bottom = 80.dp),
            isFloatingActionButtonVisible = false,
            snackbarHost = { Snackbar { Text("Сообщение") } },
        )
    }
}

internal fun calculateSnackbarBottomPadding(
    bottomNavigationInset: Dp,
    isFloatingActionButtonVisible: Boolean,
): Dp = if (isFloatingActionButtonVisible) 0.dp else bottomNavigationInset
