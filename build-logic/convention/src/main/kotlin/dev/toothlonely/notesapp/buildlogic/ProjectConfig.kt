package dev.toothlonely.notesapp.buildlogic

import org.gradle.api.JavaVersion

internal object ProjectConfig {
    const val BASE_NAMESPACE = "dev.toothlonely.notesapp"
    const val COMPILE_SDK = 37
    const val MIN_SDK = 28
    const val TARGET_SDK = 36
    const val JVM_TOOLCHAIN = 17

    val javaVersion: JavaVersion = JavaVersion.VERSION_17
}
