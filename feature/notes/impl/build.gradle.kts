plugins {
    id("notesapp.android.feature.impl")
}

dependencies {
    implementation(project(":core:data"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
