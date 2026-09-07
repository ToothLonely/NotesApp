package dev.toothlonely.notesapp.feature.notes.impl.data

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.core.content.FileProvider
import dev.toothlonely.notesapp.feature.notes.impl.domain.CameraCaptureTarget
import dev.toothlonely.notesapp.feature.notes.impl.domain.NoteImageStorage
import dev.toothlonely.notesapp.feature.notes.impl.domain.NoteImageStorageException
import dev.toothlonely.notesapp.feature.notes.impl.domain.NoteImageStorageFailure
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidNoteImageStorage(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NoteImageStorage {
    private val applicationContext = context.applicationContext
    private val contentResolver: ContentResolver = applicationContext.contentResolver
    private val fileStore = NoteImageFileStore(applicationContext.filesDir)

    override suspend fun createCameraCaptureTarget(): CameraCaptureTarget =
        withContext(ioDispatcher) {
            val captureFile = fileStore.createCameraCaptureFile()
            val uri = try {
                FileProvider.getUriForFile(
                    applicationContext,
                    "${applicationContext.packageName}$FILE_PROVIDER_AUTHORITY_SUFFIX",
                    captureFile.file,
                )
            } catch (error: RuntimeException) {
                throw NoteImageStorageException(NoteImageStorageFailure.WriteFailed, error)
            }
            CameraCaptureTarget(
                uri = uri.toString(),
                stagingFileName = captureFile.fileName,
            )
        }

    override suspend fun stageImage(
        sourceUri: String,
        disposableSourceFileName: String?,
    ): String = withContext(ioDispatcher) {
        val files = fileStore.createStageFiles()

        try {
            val uri = parseSupportedUri(sourceUri)
            validateDeclaredMimeType(uri)
            fileStore.copyBoundedSource(openSource(uri), files.incomingFile)
            normalizeImage(files.incomingFile, files.writingFile)
            fileStore.completeStage(files)
            files.fileName
        } catch (error: CancellationException) {
            throw error
        } catch (error: NoteImageStorageException) {
            throw error
        } catch (error: RuntimeException) {
            throw NoteImageStorageException(NoteImageStorageFailure.UnsupportedImage, error)
        } finally {
            fileStore.cleanupStageFiles(files, disposableSourceFileName)
        }
    }

    override suspend fun promote(stagedFileName: String) = withContext(ioDispatcher) {
        fileStore.promote(stagedFileName)
    }

    override suspend fun discardStaged(stagedFileName: String) = withContext(ioDispatcher) {
        fileStore.discardStaged(stagedFileName)
    }

    override suspend fun read(fileName: String, staged: Boolean): ByteArray =
        withContext(ioDispatcher) {
            fileStore.read(fileName, staged)
        }

    override suspend fun delete(fileName: String) = withContext(ioDispatcher) {
        fileStore.delete(fileName)
    }

    override suspend fun cleanup(
        referencedFileNames: Set<String>,
        staleStagingCutoffMillis: Long,
    ) = withContext(ioDispatcher) {
        fileStore.cleanup(referencedFileNames, staleStagingCutoffMillis)
    }

    private fun validateDeclaredMimeType(uri: Uri) {
        val mimeType = try {
            contentResolver.getType(uri)
        } catch (error: SecurityException) {
            throw NoteImageStorageException(NoteImageStorageFailure.SourceUnavailable, error)
        } ?: return
        if (!mimeType.startsWith(IMAGE_MIME_PREFIX, ignoreCase = true)) {
            throw NoteImageStorageException(NoteImageStorageFailure.UnsupportedImage)
        }
    }

    private fun openSource(uri: Uri): InputStream =
        try {
            contentResolver.openInputStream(uri)
        } catch (error: Exception) {
            throw NoteImageStorageException(NoteImageStorageFailure.SourceUnavailable, error)
        } ?: throw NoteImageStorageException(NoteImageStorageFailure.SourceUnavailable)

    private fun normalizeImage(source: File, target: File) {
        val decoded = try {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(source)) { decoder, info, _ ->
                val width = info.size.width
                val height = info.size.height
                if (
                    width <= 0 || height <= 0 ||
                    width > MAX_SOURCE_DIMENSION || height > MAX_SOURCE_DIMENSION ||
                    !info.mimeType.startsWith(IMAGE_MIME_PREFIX, ignoreCase = true)
                ) {
                    throw NoteImageStorageException(NoteImageStorageFailure.UnsupportedImage)
                }
                val scale = minOf(
                    1f,
                    MAX_STORED_DIMENSION.toFloat() / maxOf(width, height),
                )
                decoder.setTargetSize(
                    maxOf(1, (width * scale).toInt()),
                    maxOf(1, (height * scale).toInt()),
                )
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } catch (error: NoteImageStorageException) {
            throw error
        } catch (error: Exception) {
            throw NoteImageStorageException(NoteImageStorageFailure.UnsupportedImage, error)
        }

        try {
            try {
                FileOutputStream(target).use { output ->
                    if (!decoded.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) {
                        throw NoteImageStorageException(NoteImageStorageFailure.WriteFailed)
                    }
                    output.fd.sync()
                }
            } catch (error: IOException) {
                throw NoteImageStorageException(NoteImageStorageFailure.WriteFailed, error)
            }
        } finally {
            decoded.recycle()
        }
    }

    private fun parseSupportedUri(value: String): Uri {
        val uri = try {
            Uri.parse(value)
        } catch (error: RuntimeException) {
            throw NoteImageStorageException(NoteImageStorageFailure.SourceUnavailable, error)
        }
        if (uri.scheme != ContentResolver.SCHEME_CONTENT) {
            throw NoteImageStorageException(NoteImageStorageFailure.SourceUnavailable)
        }
        return uri
    }

    private companion object {
        const val FILE_PROVIDER_AUTHORITY_SUFFIX = ".note-attachments"
        const val IMAGE_MIME_PREFIX = "image/"
        const val MAX_SOURCE_DIMENSION = 32_768
        const val MAX_STORED_DIMENSION = 2_048
        const val JPEG_QUALITY = 88
    }
}
