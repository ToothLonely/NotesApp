import java.util.Properties

plugins {
    id("notesapp.android.application")
    id("notesapp.android.application.compose")
    id("notesapp.koin")
}

val localProperties = Properties().apply {
    val propertiesFile = rootProject.file("local.properties")
    if (propertiesFile.isFile) {
        propertiesFile.inputStream().use { stream -> load(stream) }
    }
}

fun String.asBuildConfigString(): String =
    "\"${replace("\\", "\\\\").replace("\"", "\\\"")}\""

android {
    namespace = "dev.toothlonely.notesapp"

    defaultConfig {
        applicationId = "dev.toothlonely.notesapp"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "GIGACHAT_ACCESS_TOKEN",
            localProperties.getProperty("access.token", "").asBuildConfigString(),
        )
        buildConfigField(
            "String",
            "GIGACHAT_AUTHORIZATION_KEY",
            localProperties.getProperty(
                "gigachat.authorization.key",
                localProperties.getProperty("authorization.key", ""),
            ).asBuildConfigString(),
        )
        buildConfigField(
            "String",
            "GIGACHAT_SCOPE",
            localProperties.getProperty("gigachat.scope", "GIGACHAT_API_PERS")
                .asBuildConfigString(),
        )
    }

    buildFeatures {
        buildConfig = true
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
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.koin.androidx.compose)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
