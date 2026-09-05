package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.icon.NotesAppIcons
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorTopBar(
    title: String,
    placeholder: String,
    titleLabel: String,
    backLabel: String,
    enabled: Boolean,
    onTitleChanged: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            InlineNoteTitleField(
                title = title,
                placeholder = placeholder,
                label = titleLabel,
                enabled = enabled,
                onTitleChanged = onTitleChanged,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = NotesAppIcons.Back,
                    contentDescription = backLabel,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun NoteEditorTopBarPreview() {
    NotesAppTheme {
        NoteEditorTopBar(
            title = "",
            placeholder = "Заголовок вашей заметки",
            titleLabel = "Заголовок заметки",
            backLabel = "Назад",
            enabled = true,
            onTitleChanged = {},
            onBack = {},
        )
    }
}
