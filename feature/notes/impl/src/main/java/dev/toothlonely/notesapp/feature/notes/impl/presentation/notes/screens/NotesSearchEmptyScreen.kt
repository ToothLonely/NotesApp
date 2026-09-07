package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.R as DesignSystemR
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme

@Composable
fun NotesSearchEmptyScreen(
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = NotesAppSpacing.space3,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        Icon(
            painter = painterResource(DesignSystemR.drawable.ic_mood_bad_24),
            contentDescription = null,
            modifier = Modifier.size(NotesAppSizes.emptyStateIcon),
            tint = MaterialTheme.colorScheme.outline,
        )
        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotesSearchEmptyScreenPreview() {
    NotesAppTheme {
        NotesSearchEmptyScreen(
            title = "Ничего не найдено",
        )
    }
}
