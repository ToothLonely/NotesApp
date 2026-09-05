package dev.toothlonely.notesapp.di

import android.content.Context
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

@OptIn(KoinExperimentalAPI::class)
class AppModuleTest {
    @Test
    fun `app Koin graph is valid`() {
        appModule.verify(extraTypes = listOf(Context::class))
    }
}
