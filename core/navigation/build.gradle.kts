import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidMultiplatformLibrary)
  alias(libs.plugins.kotlinSerialization)
}

kotlin {
  android {
    namespace = "com.akrippner.mon.core.navigation"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
    compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
  }

  jvm()

  sourceSets {
    commonMain.dependencies {
      api(libs.navigation3.runtime)
      implementation(libs.kotlinx.serialization.json)
    }
  }
}
