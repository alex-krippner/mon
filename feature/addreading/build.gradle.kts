plugins {
  id("mon.kmp.library")
  id("mon.kmp.compose")
}

kotlin {
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
