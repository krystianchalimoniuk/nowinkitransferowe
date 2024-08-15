import org.gradle.api.Plugin
import org.gradle.api.Project
import pl.nowinkitransferowe.convention.configureKotlinJvm

class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.jvm")
                apply("nowinkitransferowe.android.lint")
            }
            configureKotlinJvm()
        }
    }
}
