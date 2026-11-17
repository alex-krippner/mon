plugins {
    id("mon.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.dictionary)
            implementation(projects.core.designsystem)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
