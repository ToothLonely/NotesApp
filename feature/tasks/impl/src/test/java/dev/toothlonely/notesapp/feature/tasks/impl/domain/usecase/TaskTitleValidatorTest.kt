package dev.toothlonely.notesapp.feature.tasks.impl.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TaskTitleValidatorTest {
    private val validator = TaskTitleValidator()

    @Test
    fun `valid title is trimmed`() {
        assertEquals("Купить молоко", validator.validate("  Купить молоко  "))
    }

    @Test
    fun `blank title is rejected`() {
        assertNull(validator.validate(" \n\t "))
    }
}
