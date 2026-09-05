plugins {
    id("notesapp.android.feature.impl")
}

dependencies {
    implementation(project(":core:data"))

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
