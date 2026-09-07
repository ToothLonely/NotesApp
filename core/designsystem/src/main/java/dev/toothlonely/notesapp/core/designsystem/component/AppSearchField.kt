package dev.toothlonely.notesapp.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.R
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme

@Composable
fun AppSearchField(
    query: String,
    placeholder: String,
    searchContentDescription: String,
    clearContentDescription: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val submitSearch = {
        focusManager.clearFocus()
        onSearch()
    }
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = NotesAppSizes.searchFieldHeight),
        placeholder = { Text(text = placeholder) },
        leadingIcon = {
            IconButton(onClick = submitSearch) {
                Icon(
                    painter = painterResource(R.drawable.ic_search_24),
                    contentDescription = searchContentDescription,
                )
            }
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = onClear) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close_24),
                        contentDescription = clearContentDescription,
                    )
                }
            }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { submitSearch() }),
        singleLine = true,
        shape = MaterialTheme.shapes.extraLarge,
    )
}

@Preview(showBackground = true)
@Composable
private fun AppSearchFieldPreview() {
    NotesAppTheme {
        AppSearchField(
            query = "Покупки",
            placeholder = "Поиск заметок",
            searchContentDescription = "Найти",
            clearContentDescription = "Очистить поиск",
            onQueryChange = {},
            onSearch = {},
            onClear = {},
        )
    }
}
