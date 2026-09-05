import androidx.room.gradle.RoomExtension
import dev.toothlonely.notesapp.buildlogic.library
import dev.toothlonely.notesapp.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.google.devtools.ksp")
            pluginManager.apply("androidx.room")

            extensions.configure<RoomExtension> {
                schemaDirectory(layout.projectDirectory.dir("schemas").asFile.path)
            }

            dependencies {
                add("implementation", libs.library("androidx-room-runtime"))
                add("ksp", libs.library("androidx-room-compiler"))
            }
        }
    }
}
