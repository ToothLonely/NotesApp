plugins {
    id("notesapp.android.feature.impl")
}

dependencies {
    implementation(project(":core:data"))
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
