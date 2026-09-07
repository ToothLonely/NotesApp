package dev.toothlonely.notesapp.feature.notes.impl.domain

data class CameraCaptureTarget(
    val uri: String,
    val stagingFileName: String,
)

enum class NoteImageStorageFailure {
    SourceUnavailable,
    InputTooLarge,
    UnsupportedImage,
    WriteFailed,
}

class NoteImageStorageException(
    val failure: NoteImageStorageFailure,
    cause: Throwable? = null,
) : Exception(cause)

interface NoteImageStorage {
    suspend fun createCameraCaptureTarget(): CameraCaptureTarget

    suspend fun stageImage(
        sourceUri: String,
        disposableSourceFileName: String? = null,
    ): String

    suspend fun promote(stagedFileName: String)

    suspend fun discardStaged(stagedFileName: String)

    suspend fun read(
        fileName: String,
        staged: Boolean = false,
    ): ByteArray

    suspend fun delete(fileName: String)

    suspend fun cleanup(
        referencedFileNames: Set<String>,
        staleStagingCutoffMillis: Long,
    )
}
