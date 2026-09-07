package dev.toothlonely.notesapp.feature.notes.impl.presentation.image

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import dev.toothlonely.notesapp.feature.notes.impl.domain.repository.NoteImageStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NoteImageLoader(
    private val imageStorage: NoteImageStorage,
    private val decodeDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    suspend fun load(
        fileName: String,
        staged: Boolean,
        requestedWidth: Int,
        requestedHeight: Int,
    ): ImageBitmap? = try {
        val imageBytes = imageStorage.read(fileName, staged)
        withContext(decodeDispatcher) {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext null

            val options = BitmapFactory.Options().apply {
                inSampleSize = calculateImageSampleSize(
                    width = bounds.outWidth,
                    height = bounds.outHeight,
                    requestedWidth = requestedWidth,
                    requestedHeight = requestedHeight,
                )
            }
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)?.asImageBitmap()
        }
    } catch (error: CancellationException) {
        throw error
    } catch (_: Exception) {
        null
    }

}
