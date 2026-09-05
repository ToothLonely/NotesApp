import com.android.build.api.dsl.ApplicationExtension
import dev.toothlonely.notesapp.buildlogic.configureNotesApplication
import dev.toothlonely.notesapp.buildlogic.library
import dev.toothlonely.notesapp.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            pluginManager.apply("notesapp.android.lint")

            extensions.configure<ApplicationExtension> {
                configureNotesApplication()
            }

            dependencies {
                add("implementation", libs.library("androidx-core-ktx"))
                add("implementation", libs.library("androidx-lifecycle-runtime-ktx"))
                add("testImplementation", libs.library("junit"))
                add("testImplementation", libs.library("kotlinx-coroutines-test"))
            }
        }
    }
}
