import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.planningpoker.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        val extension = if (pluginManager.hasPlugin("com.android.application")) {
            extensions.getByType<ApplicationExtension>()
        } else {
            extensions.getByType<LibraryExtension>()
        }
        configureAndroidCompose(extension)
    }
}
