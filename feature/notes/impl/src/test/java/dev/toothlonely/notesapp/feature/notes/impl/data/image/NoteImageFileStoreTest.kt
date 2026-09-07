package dev.toothlonely.notesapp.feature.notes.impl.data.image

import dev.toothlonely.notesapp.feature.notes.impl.domain.repository.NoteImageStorageException
import dev.toothlonely.notesapp.feature.notes.impl.domain.repository.NoteImageStorageFailure
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.ArrayDeque
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class NoteImageFileStoreTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `generated files stay inside managed image directories`() {
        val store = createStore(FIRST_ID, SECOND_ID)

        val capture = store.createCameraCaptureFile()
        val stage = store.createStageFiles()
        val imageDirectory = temporaryFolder.root.resolve("note-images").canonicalFile
        val stagingDirectory = imageDirectory.resolve(".staging").canonicalFile

        assertEquals(stagingDirectory, capture.file.canonicalFile.parentFile)
        assertEquals(stagingDirectory, stage.incomingFile.canonicalFile.parentFile)
        assertEquals(stagingDirectory, stage.writingFile.canonicalFile.parentFile)
        assertEquals(stagingDirectory, stage.stagedFile.canonicalFile.parentFile)
    }

    @Test
    fun `invalid file names cannot escape managed directories`() {
        val store = createStore(FIRST_ID)

        assertThrows(IllegalArgumentException::class.java) {
            store.discardStaged("../outside.jpg")
        }
        assertThrows(IllegalArgumentException::class.java) {
            store.read("$FIRST_ID.png", staged = false)
        }
        assertFalse(temporaryFolder.root.resolve("outside.jpg").exists())
    }

    @Test
    fun `copy enforces input size and rejects empty content`() {
        val store = createStore(
            FIRST_ID,
            SECOND_ID,
            THIRD_ID,
            maxInputImageBytes = 4,
        )
        val validStage = store.createStageFiles()
        store.copyBoundedSource(ByteArrayInputStream(byteArrayOf(1, 2, 3, 4)), validStage.incomingFile)
        assertArrayEquals(byteArrayOf(1, 2, 3, 4), validStage.incomingFile.readBytes())

        val oversizedStage = store.createStageFiles()
        assertFailure(NoteImageStorageFailure.InputTooLarge) {
            store.copyBoundedSource(
                ByteArrayInputStream(byteArrayOf(1, 2, 3, 4, 5)),
                oversizedStage.incomingFile,
            )
        }

        val emptyStage = store.createStageFiles()
        assertFailure(NoteImageStorageFailure.UnsupportedImage) {
            store.copyBoundedSource(ByteArrayInputStream(byteArrayOf()), emptyStage.incomingFile)
        }
    }

    @Test
    fun `source read failure is classified separately from file write failure`() {
        val store = createStore(FIRST_ID)
        val stage = store.createStageFiles()
        val failingSource = object : InputStream() {
            override fun read(): Int = throw IOException("Source unavailable")

            override fun read(buffer: ByteArray, offset: Int, length: Int): Int =
                throw IOException("Source unavailable")
        }

        assertFailure(NoteImageStorageFailure.SourceUnavailable) {
            store.copyBoundedSource(failingSource, stage.incomingFile)
        }
    }

    @Test
    fun `staged file is promoted read and deleted`() {
        val store = createStore(FIRST_ID)
        val stage = store.createStageFiles()
        val bytes = byteArrayOf(4, 3, 2, 1)
        stage.writingFile.writeBytes(bytes)

        store.completeStage(stage)
        assertArrayEquals(bytes, store.read(stage.fileName, staged = true))

        store.promote(stage.fileName)
        assertFalse(stage.stagedFile.exists())
        assertArrayEquals(bytes, store.read(stage.fileName, staged = false))

        store.delete(stage.fileName)
        assertFailure(NoteImageStorageFailure.SourceUnavailable) {
            store.read(stage.fileName, staged = false)
        }
    }

    @Test
    fun `cleanup preserves references and recent staging while removing orphans`() {
        val store = createStore(FIRST_ID, SECOND_ID, THIRD_ID, FOURTH_ID)
        val referenced = store.createStageFiles().also { stage ->
            completeWithContent(store, stage)
        }
        store.promote(referenced.fileName)
        val orphan = store.createStageFiles().also { stage ->
            completeWithContent(store, stage)
        }
        store.promote(orphan.fileName)
        val staleStage = store.createStageFiles().also { stage ->
            completeWithContent(store, stage)
        }
        val recentStage = store.createStageFiles().also { stage ->
            completeWithContent(store, stage)
        }
        assertTrue(staleStage.stagedFile.setLastModified(100))
        assertTrue(recentStage.stagedFile.setLastModified(300))

        store.cleanup(
            referencedFileNames = setOf(referenced.fileName, "../outside.jpg"),
            staleStagingCutoffMillis = 200,
        )

        assertArrayEquals(TEST_CONTENT, store.read(referenced.fileName, staged = false))
        assertFailure(NoteImageStorageFailure.SourceUnavailable) {
            store.read(orphan.fileName, staged = false)
        }
        assertFailure(NoteImageStorageFailure.SourceUnavailable) {
            store.read(staleStage.fileName, staged = true)
        }
        assertArrayEquals(TEST_CONTENT, store.read(recentStage.fileName, staged = true))
    }

    @Test
    fun `stage cleanup removes temporary and disposable camera files`() {
        val store = createStore(FIRST_ID, SECOND_ID)
        val capture = store.createCameraCaptureFile()
        capture.file.writeBytes(TEST_CONTENT)
        val stage = store.createStageFiles()
        stage.incomingFile.writeBytes(TEST_CONTENT)
        stage.writingFile.writeBytes(TEST_CONTENT)

        store.cleanupStageFiles(stage, capture.fileName)

        assertFalse(capture.file.exists())
        assertFalse(stage.incomingFile.exists())
        assertFalse(stage.writingFile.exists())
    }

    @Test
    fun `stored file size is enforced before and after promotion`() {
        val store = createStore(
            FIRST_ID,
            maxStoredImageBytes = 3,
        )
        val stage = store.createStageFiles()
        stage.writingFile.writeBytes(byteArrayOf(1, 2, 3, 4))

        assertFailure(NoteImageStorageFailure.WriteFailed) {
            store.completeStage(stage)
        }
    }

    private fun createStore(
        vararg ids: String,
        maxInputImageBytes: Long = 25L * 1024L * 1024L,
        maxStoredImageBytes: Long = 12L * 1024L * 1024L,
    ): NoteImageFileStore {
        val remainingIds = ArrayDeque(ids.toList())
        return NoteImageFileStore(
            filesDirectory = temporaryFolder.root,
            fileIdFactory = { remainingIds.removeFirst() },
            maxInputImageBytes = maxInputImageBytes,
            maxStoredImageBytes = maxStoredImageBytes,
        )
    }

    private fun completeWithContent(
        store: NoteImageFileStore,
        stage: NoteImageFileStore.StageFiles,
    ) {
        stage.writingFile.writeBytes(TEST_CONTENT)
        store.completeStage(stage)
    }

    private fun assertFailure(
        expected: NoteImageStorageFailure,
        action: () -> Unit,
    ) {
        val error = assertThrows(NoteImageStorageException::class.java, action)
        assertEquals(expected, error.failure)
    }

    private companion object {
        const val FIRST_ID = "00000000-0000-0000-0000-000000000001"
        const val SECOND_ID = "00000000-0000-0000-0000-000000000002"
        const val THIRD_ID = "00000000-0000-0000-0000-000000000003"
        const val FOURTH_ID = "00000000-0000-0000-0000-000000000004"
        val TEST_CONTENT = byteArrayOf(1, 2, 3)
    }
}
