import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLintConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.withPlugin("com.android.application") {
                extensions.configure<ApplicationExtension> {
                    lint {
                        abortOnError = true
                        checkDependencies = true
                        warningsAsErrors = true
                    }
                }
            }
            pluginManager.withPlugin("com.android.library") {
                extensions.configure<LibraryExtension> {
                    lint {
                        abortOnError = true
                        checkDependencies = true
                        warningsAsErrors = true
                    }
                }
            }
        }
    }
}
