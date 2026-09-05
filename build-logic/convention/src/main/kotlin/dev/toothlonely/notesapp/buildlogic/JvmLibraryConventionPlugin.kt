import dev.toothlonely.notesapp.buildlogic.ProjectConfig
import dev.toothlonely.notesapp.buildlogic.library
import dev.toothlonely.notesapp.buildlogic.libs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")

            extensions.configure<JavaPluginExtension> {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
                toolchain.languageVersion.set(JavaLanguageVersion.of(ProjectConfig.JVM_TOOLCHAIN))
            }
            extensions.configure<KotlinJvmProjectExtension> {
                jvmToolchain(ProjectConfig.JVM_TOOLCHAIN)
            }

            dependencies {
                add("implementation", libs.library("kotlinx-coroutines-core"))
                add("testImplementation", libs.library("junit"))
                add("testImplementation", libs.library("kotlinx-coroutines-test"))
            }
        }
    }
}
