import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/** Adds the browser (Kotlin/Wasm) target to a module. Needs `mon.kmp.library` applied first. */
class KmpWebConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      extensions.configure<KotlinMultiplatformExtension> {
        @OptIn(ExperimentalWasmDsl::class) wasmJs { browser() }
      }
    }
  }
}
