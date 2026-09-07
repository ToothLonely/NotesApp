plugins {
    id("notesapp.android.application")
    id("notesapp.android.application.compose")
    id("notesapp.koin")
}

android {
    namespace = "dev.toothlonely.notesapp"

    defaultConfig {
        applicationId = "dev.toothlonely.notesapp"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":feature:notes:api"))
    implementation(project(":feature:notes:impl"))
    implementation(project(":feature:settings:api"))
    implementation(project(":feature:settings:impl"))
    implementation(project(":feature:tasks:api"))
    implementation(project(":feature:tasks:impl"))

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
