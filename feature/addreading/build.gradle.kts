plugins {
  id("mon.feature")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.core.domain)
    }

    commonTest.dependencies {
      implementation(kotlin("test"))
      implementation(libs.kotlinx.coroutines.test)
      implementation(projects.core.data)
      implementation(projects.core.tokenizer)
    }
  }
}
