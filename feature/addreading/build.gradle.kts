import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidMultiplatformLibrary)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
}

kotlin {
  android {
    namespace = "com.akrippner.mon.feature.addreading"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
    compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
  }

  jvm()

  sourceSets {
    commonMain.dependencies {
      implementation(projects.core.domain)
      implementation(projects.core.model)
      api(projects.core.arch)

      implementation(libs.compose.runtime)
      implementation(libs.compose.foundation)
      implementation(libs.compose.material3)
      implementation(libs.compose.ui)
      implementation(libs.compose.uiToolingPreview)

      implementation(libs.koin.core)
      implementation(libs.koin.compose.viewmodel)
      implementation(libs.androidx.lifecycle.viewmodelCompose)
    }

    commonTest.dependencies {
      implementation(kotlin("test"))
      implementation(libs.kotlinx.coroutines.test)
      implementation(projects.core.data)
      implementation(projects.core.tokenizer)
    }
  }
}
