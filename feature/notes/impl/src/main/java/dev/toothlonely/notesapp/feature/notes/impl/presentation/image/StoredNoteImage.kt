package dev.toothlonely.notesapp.feature.notes.impl.presentation.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import dev.toothlonely.notesapp.core.designsystem.R as DesignSystemR
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme

@Composable
fun StoredNoteImage(
    fileName: String?,
    staged: Boolean,
    contentDescription: String?,
    loadImage: suspend (String, Boolean, Int, Int) -> ImageBitmap?,
    onLoadError: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var requestedSize by remember { mutableStateOf(IntSize.Zero) }
    val bitmap by produceState<ImageBitmap?>(
        initialValue = null,
        fileName,
        staged,
        requestedSize,
    ) {
        value = if (
            fileName != null &&
            requestedSize.width > 0 &&
            requestedSize.height > 0
        ) {
            loadImage(fileName, staged, requestedSize.width, requestedSize.height).also { image ->
                if (image == null) onLoadError()
            }
        } else {
            null
        }
    }

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .onSizeChanged { requestedSize = it },
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap == null) {
            Icon(
                painter = painterResource(DesignSystemR.drawable.ic_note_24),
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Image(
                bitmap = checkNotNull(bitmap),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StoredNoteImagePreview() {
    NotesAppTheme {
        StoredNoteImage(
            fileName = null,
            staged = false,
            contentDescription = "Изображение",
            loadImage = { _, _, _, _ -> null },
        )
    }
}
