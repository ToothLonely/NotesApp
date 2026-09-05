package dev.toothlonely.notesapp.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.library(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).orElseThrow {
        IllegalStateException("Library alias '$alias' is not declared in the version catalog")
    }

internal fun Project.namespaceFromPath(): String =
    buildString {
        append(ProjectConfig.BASE_NAMESPACE)
        path.split(':')
            .filter(String::isNotBlank)
            .forEach { segment ->
                append('.')
                append(segment.replace('-', '.'))
            }
    }
