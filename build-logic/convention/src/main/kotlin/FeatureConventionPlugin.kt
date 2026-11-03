import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.resources.ResourcesExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Applies the library and Compose plugins and adds the dependencies every feature module shares.
 */
class FeatureConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply(KmpLibraryConventionPlugin::class.java)
      pluginManager.apply(KmpComposeConventionPlugin::class.java)

      val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

      extensions.configure<KotlinMultiplatformExtension> {
        sourceSets.getByName("commonMain").dependencies {
          // BaseViewModel is a supertype of the features' public view models.
          api(project(":core:arch"))
          implementation(project(":core:model"))

          implementation(libs.findLibrary("compose-runtime").get())
          implementation(libs.findLibrary("compose-components-resources").get())
          implementation(libs.findLibrary("koin-core").get())
          implementation(libs.findLibrary("koin-compose").get())
          implementation(libs.findLibrary("koin-compose-viewmodel").get())
          implementation(libs.findLibrary("androidx-lifecycle-viewmodelCompose").get())
        }
      }

      // Each feature owns its strings, e.g. com.akrippner.mon.feature.library.generated.resources.
      extensions.configure<ComposeExtension> {
        (this as ExtensionAware).extensions.configure<ResourcesExtension>("resources") {
          packageOfResClass =
            "com.akrippner.mon." + path.removePrefix(":").replace(":", ".") + ".generated.resources"
        }
      }
    }
  }
}
