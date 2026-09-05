import com.android.build.api.dsl.ApplicationExtension
import dev.toothlonely.notesapp.buildlogic.library
import dev.toothlonely.notesapp.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidApplicationComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.configure<ApplicationExtension> {
                buildFeatures.compose = true
            }

            dependencies {
                add("implementation", platform(libs.library("androidx-compose-bom")))
                add("implementation", libs.library("androidx-activity-compose"))
                add("implementation", libs.library("androidx-compose-material3"))
                add("implementation", libs.library("androidx-compose-ui"))
                add("implementation", libs.library("androidx-compose-ui-graphics"))
                add("implementation", libs.library("androidx-compose-ui-tooling-preview"))
                add("debugImplementation", libs.library("androidx-compose-ui-tooling"))
            }
        }
    }
}
