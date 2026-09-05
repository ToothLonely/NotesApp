import com.android.build.api.dsl.LibraryExtension
import dev.toothlonely.notesapp.buildlogic.configureNotesLibrary
import dev.toothlonely.notesapp.buildlogic.library
import dev.toothlonely.notesapp.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            pluginManager.apply("notesapp.android.lint")

            extensions.configure<LibraryExtension> {
                configureNotesLibrary(project)
            }

            dependencies {
                add("testImplementation", libs.library("junit"))
                add("testImplementation", libs.library("kotlinx-coroutines-test"))
            }
        }
    }
}
