package dev.toothlonely.notesapp.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.R
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme

@Composable
fun SortMenu(
    newestFirst: Boolean,
    sortContentDescription: String,
    newestFirstLabel: String,
    oldestFirstLabel: String,
    onNewestFirst: () -> Unit,
    onOldestFirst: () -> Unit,
    enabled: Boolean = true,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = { expanded = true },
            enabled = enabled,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_swap_vert_24),
                contentDescription = sortContentDescription,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(text = newestFirstLabel) },
                onClick = {
                    expanded = false
                    onNewestFirst()
                },
                trailingIcon = {
                    RadioButton(
                        selected = newestFirst,
                        onClick = null,
                    )
                },
            )
            DropdownMenuItem(
                text = { Text(text = oldestFirstLabel) },
                onClick = {
                    expanded = false
                    onOldestFirst()
                },
                trailingIcon = {
                    RadioButton(
                        selected = !newestFirst,
                        onClick = null,
                    )
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SortMenuPreview() {
    NotesAppTheme {
        SortMenu(
            newestFirst = true,
            sortContentDescription = "Сортировать",
            newestFirstLabel = "Сначала новые",
            oldestFirstLabel = "Сначала старые",
            onNewestFirst = {},
            onOldestFirst = {},
        )
    }
}
