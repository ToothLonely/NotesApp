package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.R as DesignSystemR
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesTopBar(
    title: String,
    isDeleteMode: Boolean,
    deleteActionEnabled: Boolean,
    enterDeleteModeLabel: String,
    exitDeleteModeLabel: String,
    onToggleDeleteMode: () -> Unit,
) {
    TopAppBar(
        title = { Text(text = title) },
        actions = {
            IconButton(
                onClick = onToggleDeleteMode,
                enabled = deleteActionEnabled,
            ) {
                Icon(
                    painter = painterResource(
                        if (isDeleteMode) {
                            DesignSystemR.drawable.ic_close_24
                        } else {
                            DesignSystemR.drawable.ic_delete_24
                        },
                    ),
                    contentDescription = if (isDeleteMode) {
                        exitDeleteModeLabel
                    } else {
                        enterDeleteModeLabel
                    },
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun NotesTopBarPreview() {
    NotesAppTheme {
        NotesTopBar(
            title = "Заметки",
            isDeleteMode = false,
            deleteActionEnabled = true,
            enterDeleteModeLabel = "Включить режим удаления",
            exitDeleteModeLabel = "Выйти из режима удаления",
            onToggleDeleteMode = {},
        )
    }
}
