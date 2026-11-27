plugins {
  id("mon.feature")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.core.domain)
      implementation(projects.core.designsystem)
      implementation(libs.compose.foundation)
      implementation(libs.compose.material3)
      implementation(libs.compose.ui)
      implementation(libs.navigationevent.compose)
    }

    // The system file picker is platform code: see file/TextFilePicker.kt.
    androidMain.dependencies {
      implementation(libs.androidx.activity.compose)
    }

    commonTest.dependencies {
      implementation(kotlin("test"))
      implementation(libs.kotlinx.coroutines.test)
      implementation(projects.core.data)
      implementation(projects.core.tokenizer)
    }
  }
}
