import dev.toothlonely.notesapp.buildlogic.library
import dev.toothlonely.notesapp.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureApiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("notesapp.android.library")
            pluginManager.apply("notesapp.kotlin.serialization")

            dependencies {
                add("api", libs.library("androidx-navigation3-runtime"))
            }
        }
    }
}
