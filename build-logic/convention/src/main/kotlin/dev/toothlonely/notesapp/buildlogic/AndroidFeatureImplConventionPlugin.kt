import dev.toothlonely.notesapp.buildlogic.library
import dev.toothlonely.notesapp.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureImplConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("notesapp.android.library")
            pluginManager.apply("notesapp.android.library.compose")
            pluginManager.apply("notesapp.koin")

            require(path.endsWith(IMPLEMENTATION_MODULE_SUFFIX)) {
                "Plugin notesapp.android.feature.impl can only be applied to an :impl module"
            }
            val featureApiPath = path.removeSuffix(IMPLEMENTATION_MODULE_SUFFIX) + API_MODULE_SUFFIX

            dependencies {
                add("implementation", project(featureApiPath))
                add("implementation", project(":core:designsystem"))
                add("implementation", project(":core:domain"))
                add("implementation", libs.library("androidx-lifecycle-runtime-compose"))
                add("implementation", libs.library("androidx-lifecycle-viewmodel-compose"))
                add("implementation", libs.library("androidx-lifecycle-viewmodel-navigation3"))
                add("implementation", libs.library("androidx-navigation3-ui"))
                add("implementation", libs.library("koin-androidx-compose"))
                add("implementation", libs.library("koin-compose-navigation3"))
                add("implementation", libs.library("kotlinx-coroutines-android"))
            }
        }
    }

    private companion object {
        const val API_MODULE_SUFFIX = ":api"
        const val IMPLEMENTATION_MODULE_SUFFIX = ":impl"
    }
}
