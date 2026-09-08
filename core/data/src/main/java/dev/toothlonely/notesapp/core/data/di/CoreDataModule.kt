package dev.toothlonely.notesapp.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import dev.toothlonely.notesapp.core.data.database.NotesAppDatabase
import dev.toothlonely.notesapp.core.data.database.notesMigration1To2
import dev.toothlonely.notesapp.core.data.database.notesMigration2To3
import dev.toothlonely.notesapp.core.data.database.notesMigration3To4
import dev.toothlonely.notesapp.core.data.database.notesMigration4To5
import dev.toothlonely.notesapp.core.data.network.gigachat.ConfigurableGigaChatTokenProvider
import dev.toothlonely.notesapp.core.data.network.gigachat.GigaChatApi
import dev.toothlonely.notesapp.core.data.network.gigachat.GigaChatAuthApi
import dev.toothlonely.notesapp.core.data.network.gigachat.GigaChatAuthDataSource
import dev.toothlonely.notesapp.core.data.network.gigachat.GigaChatNetworkDataSource
import dev.toothlonely.notesapp.core.data.network.gigachat.GigaChatTokenProvider
import dev.toothlonely.notesapp.core.data.network.gigachat.RetrofitGigaChatAuthDataSource
import dev.toothlonely.notesapp.core.data.network.gigachat.RetrofitGigaChatNetworkDataSource
import dev.toothlonely.notesapp.core.data.preferences.DataStoreUserPreferencesRepository
import dev.toothlonely.notesapp.core.data.preferences.UserPreferencesDataSource
import dev.toothlonely.notesapp.core.data.repository.NetworkGigaChatRepository
import dev.toothlonely.notesapp.core.data.speech.AndroidSpeechRecognitionRepository
import dev.toothlonely.notesapp.core.domain.repository.GigaChatRepository
import dev.toothlonely.notesapp.core.domain.speech.SpeechRecognitionRepository
import dev.toothlonely.notesapp.core.domain.repository.UserPreferencesRepository
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit

private const val NOTES_DATABASE_NAME = "notes-app.db"
private const val USER_PREFERENCES_NAME = "notes-app-preferences"
private const val GIGACHAT_API_BASE_URL = "https://api.giga.chat/v1/"
private const val GIGACHAT_AUTH_BASE_URL = "https://ngw.devices.sberbank.ru:9443/"
private const val NETWORK_CONNECT_TIMEOUT_SECONDS = 15L
private const val NETWORK_READ_TIMEOUT_SECONDS = 60L
private const val NETWORK_WRITE_TIMEOUT_SECONDS = 30L

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCES_NAME,
)

val coreDataModule = module {
    single<DataStore<Preferences>> { androidContext().userPreferencesDataStore }
    single { UserPreferencesDataSource(get()) }
    single<UserPreferencesRepository> { DataStoreUserPreferencesRepository(get()) }
    single {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }
    single {
        OkHttpClient.Builder()
            .connectTimeout(NETWORK_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NETWORK_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(NETWORK_WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }
    single<GigaChatApi> {
        Retrofit.Builder()
            .baseUrl(GIGACHAT_API_BASE_URL)
            .client(get())
            .build()
            .create(GigaChatApi::class.java)
    }
    single<GigaChatAuthApi> {
        Retrofit.Builder()
            .baseUrl(GIGACHAT_AUTH_BASE_URL)
            .client(get())
            .build()
            .create(GigaChatAuthApi::class.java)
    }
    single<GigaChatAuthDataSource> {
        RetrofitGigaChatAuthDataSource(
            api = get(),
            json = get(),
            requestIdProvider = { UUID.randomUUID().toString() },
        )
    }
    single<GigaChatTokenProvider> {
        ConfigurableGigaChatTokenProvider(
            credentials = get(),
            authDataSource = get(),
            currentTimeMillis = System::currentTimeMillis,
        )
    }
    single<GigaChatNetworkDataSource> { RetrofitGigaChatNetworkDataSource(get(), get()) }
    single<GigaChatRepository> { NetworkGigaChatRepository(get(), get()) }
    factory<SpeechRecognitionRepository> {
        AndroidSpeechRecognitionRepository(androidContext())
    }
    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = NotesAppDatabase::class.java,
            name = NOTES_DATABASE_NAME,
        )
            .addMigrations(
                notesMigration1To2,
                notesMigration2To3,
                notesMigration3To4,
                notesMigration4To5,
            )
            .build()
    }
    single { get<NotesAppDatabase>().notesDao() }
    single { get<NotesAppDatabase>().tasksDao() }
}
