package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.R as DesignSystemR
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.tasks.impl.R

@Composable
fun TaskOverflowMenu(
    menuContentDescription: String,
    editLabel: String,
    deleteLabel: String,
    editContentDescription: String,
    deleteContentDescription: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    enabled: Boolean,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = { expanded = true },
            enabled = enabled,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_more_vert_24),
                contentDescription = menuContentDescription,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                modifier = Modifier.semantics {
                    contentDescription = editContentDescription
                },
                text = { Text(text = editLabel) },
                onClick = {
                    expanded = false
                    onEdit()
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(DesignSystemR.drawable.ic_edit_24),
                        contentDescription = null,
                    )
                },
            )
            DropdownMenuItem(
                modifier = Modifier.semantics {
                    contentDescription = deleteContentDescription
                },
                text = {
                    Text(
                        text = deleteLabel,
                        color = MaterialTheme.colorScheme.error,
                    )
                },
                onClick = {
                    expanded = false
                    onDelete()
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(DesignSystemR.drawable.ic_delete_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskOverflowMenuPreview() {
    NotesAppTheme {
        TaskOverflowMenu(
            menuContentDescription = "Действия задачи",
            editLabel = "Редактировать",
            deleteLabel = "Удалить",
            editContentDescription = "Редактировать задачу «Купить молоко»",
            deleteContentDescription = "Удалить задачу «Купить молоко»",
            onEdit = {},
            onDelete = {},
            enabled = true,
        )
    }
}
