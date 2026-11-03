import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("org.jetbrains.kotlin.multiplatform")
      pluginManager.apply("com.android.kotlin.multiplatform.library")

      val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

      extensions.configure<KotlinMultiplatformExtension> {
        jvm()
        targets.withType(KotlinMultiplatformAndroidLibraryTarget::class.java).configureEach {
          namespace = "com.akrippner.mon." + target.path.removePrefix(":").replace(":", ".")
          minSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()
          compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
          compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
        }
      }
    }
  }
}
