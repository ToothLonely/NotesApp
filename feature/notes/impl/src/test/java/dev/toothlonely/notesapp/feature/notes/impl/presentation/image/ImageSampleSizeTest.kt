package dev.toothlonely.notesapp.feature.notes.impl.presentation.image

import org.junit.Assert.assertEquals
import org.junit.Test

class ImageSampleSizeTest {
    @Test
    fun `large image is sampled by a power of two`() {
        assertEquals(
            4,
            calculateImageSampleSize(
                width = 4_000,
                height = 3_000,
                requestedWidth = 800,
                requestedHeight = 600,
            ),
        )
    }

    @Test
    fun `small image keeps original sample size`() {
        assertEquals(
            1,
            calculateImageSampleSize(
                width = 640,
                height = 480,
                requestedWidth = 800,
                requestedHeight = 600,
            ),
        )
    }

    @Test
    fun `invalid requested dimensions are clamped`() {
        assertEquals(
            1_024,
            calculateImageSampleSize(
                width = 1_024,
                height = 1_024,
                requestedWidth = 0,
                requestedHeight = -1,
            ),
        )
    }
}
