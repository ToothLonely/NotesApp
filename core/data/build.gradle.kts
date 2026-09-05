plugins {
    id("notesapp.android.library")
    id("notesapp.android.room")
    id("notesapp.kotlin.serialization")
    id("notesapp.koin")
}

dependencies {
    implementation(project(":core:domain"))

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
}
