import com.android.build.api.dsl.LibraryExtension
import com.google.devtools.ksp.gradle.KspExtension
import com.planningpoker.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.google.devtools.ksp")

        val schemaDir = layout.projectDirectory.dir("schemas")
        extensions.configure<KspExtension> {
            // Exported schemas back the auto-migrations and their tests.
            arg("room.schemaLocation", schemaDir.asFile.path)
        }
        extensions.configure<LibraryExtension> {
            // MigrationTestHelper reads the exported schemas from test assets.
            sourceSets.getByName("androidTest").assets.srcDir(schemaDir)
        }
        dependencies {
            add("implementation", libs.findLibrary("room-runtime").get())
            add("implementation", libs.findLibrary("room-ktx").get())
            add("ksp", libs.findLibrary("room-compiler").get())
            add("androidTestImplementation", libs.findLibrary("room-testing").get())
        }
    }
}
