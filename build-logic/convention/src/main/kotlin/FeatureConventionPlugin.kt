import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
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

      // Declared dependencies are only known once the module's own build script has run.
      afterEvaluate { checkFeatureDependencies() }

      // Each feature owns its strings, e.g. com.akrippner.mon.feature.library.generated.resources.
      extensions.configure<ComposeExtension> {
        (this as ExtensionAware).extensions.configure<ResourcesExtension>("resources") {
          packageOfResClass =
            "com.akrippner.mon." + path.removePrefix(":").replace(":", ".") + ".generated.resources"
        }
      }
    }
  }

  /**
   * Fails the build when a feature declares a project dependency it must not have: `:core:database`
   * (features reach persistence through `:core:data`) or another feature (features are wired
   * together only in `:shared`). Only declared dependencies count, so a transitive path through
   * `:core:data` is fine.
   *
   * TODO: Look into adopting libraries that guard the architecture instead of this hand-written
   *   check, e.g. Module Graph Assert for module dependency rules and Konsist for source-level
   *   rules.
   */
  private fun Project.checkFeatureDependencies() {
    val violations =
      configurations
        .flatMap { configuration ->
          configuration.dependencies.withType(ProjectDependency::class.java).mapNotNull { dependency
            ->
            val reason =
              when {
                dependency.path == DATABASE_MODULE -> "must not depend on $DATABASE_MODULE"
                dependency.path.startsWith(FEATURE_PREFIX) && dependency.path != path ->
                  "must not depend on another feature"
                else -> null
              }
            reason?.let { "$path $it: ${dependency.path} (in '${configuration.name}')" }
          }
        }
        .distinct()
    if (violations.isNotEmpty()) {
      throw GradleException(violations.joinToString(separator = "\n", prefix = "Module rules:\n"))
    }
  }

  private companion object {
    const val DATABASE_MODULE = ":core:database"
    const val FEATURE_PREFIX = ":feature:"
  }
}
