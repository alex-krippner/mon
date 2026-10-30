
plugins {
id("mon.kmp.library")
  alias(libs.plugins.kotlinSerialization)
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.navigation3.runtime)
      implementation(libs.kotlinx.serialization.json)
    }
  }
}
