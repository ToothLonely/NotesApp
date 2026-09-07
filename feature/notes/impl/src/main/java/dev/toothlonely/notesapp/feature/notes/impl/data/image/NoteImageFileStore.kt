package dev.toothlonely.notesapp.feature.notes.impl.data.image

import dev.toothlonely.notesapp.feature.notes.impl.domain.repository.NoteImageStorageException
import dev.toothlonely.notesapp.feature.notes.impl.domain.repository.NoteImageStorageFailure
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID

internal class NoteImageFileStore(
    filesDirectory: File,
    private val fileIdFactory: () -> String = { UUID.randomUUID().toString() },
    private val maxInputImageBytes: Long = DEFAULT_MAX_INPUT_IMAGE_BYTES,
    private val maxStoredImageBytes: Long = DEFAULT_MAX_STORED_IMAGE_BYTES,
) {
    private val rootDirectory = filesDirectory
    private val imageDirectory = File(filesDirectory, IMAGE_DIRECTORY_NAME)
    private val stagingDirectory = File(imageDirectory, STAGING_DIRECTORY_NAME)

    fun createCameraCaptureFile(): NamedFile {
        ensureDirectories()
        val fileName = "$CAPTURE_FILE_PREFIX${newFileId()}$IMAGE_FILE_SUFFIX"
        val file = resolveStagingFile(fileName)
        if (file.exists()) {
            throw NoteImageStorageException(NoteImageStorageFailure.WriteFailed)
        }
        return NamedFile(fileName, file)
    }

    fun createStageFiles(): StageFiles {
        ensureDirectories()
        val fileName = "${newFileId()}$IMAGE_FILE_SUFFIX"
        return StageFiles(
            fileName = fileName,
            incomingFile = resolveStagingFile("$fileName$INCOMING_FILE_SUFFIX"),
            writingFile = resolveStagingFile("$fileName$WRITING_FILE_SUFFIX"),
            stagedFile = resolveStagingFile(fileName),
        )
    }

    fun copyBoundedSource(source: InputStream, target: File) {
        try {
            source.use { input ->
                FileOutputStream(target).use { output ->
                    val buffer = ByteArray(COPY_BUFFER_SIZE)
                    var totalBytes = 0L
                    while (true) {
                        val readCount = try {
                            input.read(buffer)
                        } catch (error: IOException) {
                            throw NoteImageStorageException(
                                NoteImageStorageFailure.SourceUnavailable,
                                error,
                            )
                        }
                        if (readCount < 0) break
                        totalBytes += readCount
                        if (totalBytes > maxInputImageBytes) {
                            throw NoteImageStorageException(
                                NoteImageStorageFailure.InputTooLarge,
                            )
                        }
                        output.write(buffer, 0, readCount)
                    }
                    output.fd.sync()
                }
            }
        } catch (error: NoteImageStorageException) {
            throw error
        } catch (error: IOException) {
            throw NoteImageStorageException(NoteImageStorageFailure.WriteFailed, error)
        }
        if (target.length() == 0L) {
            throw NoteImageStorageException(NoteImageStorageFailure.UnsupportedImage)
        }
    }

    fun completeStage(files: StageFiles) {
        validateStoredFile(files.writingFile)
        try {
            moveAtomically(files.writingFile, files.stagedFile)
        } catch (error: IOException) {
            throw NoteImageStorageException(NoteImageStorageFailure.WriteFailed, error)
        }
    }

    fun cleanupStageFiles(
        files: StageFiles,
        disposableSourceFileName: String?,
    ) {
        files.incomingFile.delete()
        files.writingFile.delete()
        if (
            disposableSourceFileName != null &&
            STAGING_FILE_NAME.matches(disposableSourceFileName)
        ) {
            try {
                resolveStagingFile(disposableSourceFileName).delete()
            } catch (_: Exception) {
                Unit
            }
        }
    }

    fun promote(stagedFileName: String) {
        ensureDirectories()
        val source = resolveStagedImageFile(stagedFileName)
        val target = resolvePermanentImageFile(stagedFileName)
        if (!source.isFile) {
            throw NoteImageStorageException(NoteImageStorageFailure.SourceUnavailable)
        }
        try {
            moveAtomically(source, target)
        } catch (error: IOException) {
            throw NoteImageStorageException(NoteImageStorageFailure.WriteFailed, error)
        }
    }

    fun discardStaged(stagedFileName: String) {
        ensureDirectories()
        val file = resolveStagingFile(stagedFileName)
        if (file.exists() && !file.delete()) {
            throw IOException("Could not remove staged note image")
        }
    }

    fun read(fileName: String, staged: Boolean): ByteArray {
        ensureDirectories()
        val file = if (staged) {
            resolveStagedImageFile(fileName)
        } else {
            resolvePermanentImageFile(fileName)
        }
        if (!file.isFile || file.length() > maxStoredImageBytes) {
            throw NoteImageStorageException(NoteImageStorageFailure.SourceUnavailable)
        }
        return try {
            file.readBytes()
        } catch (error: IOException) {
            throw NoteImageStorageException(NoteImageStorageFailure.SourceUnavailable, error)
        }
    }

    fun delete(fileName: String) {
        ensureDirectories()
        val file = resolvePermanentImageFile(fileName)
        if (file.exists() && !file.delete()) {
            throw IOException("Could not remove note image")
        }
    }

    fun cleanup(
        referencedFileNames: Set<String>,
        staleStagingCutoffMillis: Long,
    ) {
        ensureDirectories()
        val validReferences = referencedFileNames.filterTo(mutableSetOf()) { fileName ->
            PERSISTENT_FILE_NAME.matches(fileName)
        }
        imageDirectory.listFiles().orEmpty()
            .filter(File::isFile)
            .filter { file -> file.name !in validReferences }
            .forEach(File::delete)
        stagingDirectory.listFiles().orEmpty()
            .filter(File::isFile)
            .filter { file -> file.lastModified() < staleStagingCutoffMillis }
            .forEach(File::delete)
    }

    private fun validateStoredFile(file: File) {
        if (file.length() == 0L || file.length() > maxStoredImageBytes) {
            throw NoteImageStorageException(NoteImageStorageFailure.WriteFailed)
        }
    }

    private fun moveAtomically(source: File, target: File) {
        if (target.exists()) throw IOException("Target note image already exists")
        Files.move(source.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE)
    }

    private fun ensureDirectories() {
        try {
            if (!imageDirectory.exists() && !imageDirectory.mkdirs()) {
                throw NoteImageStorageException(NoteImageStorageFailure.WriteFailed)
            }
            val canonicalImageDirectory = imageDirectory.canonicalFile
            if (canonicalImageDirectory.parentFile != rootDirectory.canonicalFile) {
                throw NoteImageStorageException(NoteImageStorageFailure.WriteFailed)
            }
            if (!stagingDirectory.exists() && !stagingDirectory.mkdirs()) {
                throw NoteImageStorageException(NoteImageStorageFailure.WriteFailed)
            }
            if (stagingDirectory.canonicalFile.parentFile != canonicalImageDirectory) {
                throw NoteImageStorageException(NoteImageStorageFailure.WriteFailed)
            }
        } catch (error: NoteImageStorageException) {
            throw error
        } catch (error: IOException) {
            throw NoteImageStorageException(NoteImageStorageFailure.WriteFailed, error)
        }
    }

    private fun newFileId(): String = fileIdFactory().also { fileId ->
        require(UUID_PART.matches(fileId))
    }

    private fun resolvePermanentImageFile(fileName: String): File {
        require(PERSISTENT_FILE_NAME.matches(fileName))
        return resolveChild(imageDirectory, fileName)
    }

    private fun resolveStagedImageFile(fileName: String): File {
        require(PERSISTENT_FILE_NAME.matches(fileName))
        return resolveChild(stagingDirectory, fileName)
    }

    private fun resolveStagingFile(fileName: String): File {
        require(STAGING_FILE_NAME.matches(fileName))
        return resolveChild(stagingDirectory, fileName)
    }

    private fun resolveChild(parent: File, fileName: String): File {
        val canonicalParent = parent.canonicalFile
        val candidate = File(canonicalParent, fileName).canonicalFile
        require(candidate.parentFile == canonicalParent)
        return candidate
    }

    data class NamedFile(
        val fileName: String,
        val file: File,
    )

    data class StageFiles(
        val fileName: String,
        val incomingFile: File,
        val writingFile: File,
        val stagedFile: File,
    )

    private companion object {
        const val IMAGE_DIRECTORY_NAME = "note-images"
        const val STAGING_DIRECTORY_NAME = ".staging"
        const val IMAGE_FILE_SUFFIX = ".jpg"
        const val CAPTURE_FILE_PREFIX = "capture-"
        const val INCOMING_FILE_SUFFIX = ".incoming"
        const val WRITING_FILE_SUFFIX = ".writing"
        const val COPY_BUFFER_SIZE = 8 * 1024
        const val DEFAULT_MAX_INPUT_IMAGE_BYTES = 25L * 1024L * 1024L
        const val DEFAULT_MAX_STORED_IMAGE_BYTES = 12L * 1024L * 1024L
        val UUID_PART = Regex(
            "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}",
        )
        val PERSISTENT_FILE_NAME = Regex("^${UUID_PART.pattern}\\.jpg$")
        val STAGING_FILE_NAME = Regex(
            "^(?:capture-)?${UUID_PART.pattern}\\.jpg(?:\\.incoming|\\.writing)?$",
        )
    }
}
