plugins {
    id("mon.kmp.library")
    id("mon.kmp.compose")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.compose.runtime)
                api(libs.compose.foundation)
                api(libs.compose.material3)
                api(libs.compose.ui)

                implementation(libs.compose.uiToolingPreview)
                implementation(projects.core.designsystem)
            }
        }
    }
}
