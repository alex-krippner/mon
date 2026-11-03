plugins {
    id("mon.kmp.library")
    id("mon.kmp.compose")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core.data)
                implementation(projects.core.designsystem)
                api(projects.core.arch)
                implementation(projects.core.model)
                api(libs.compose.runtime)
                api(libs.compose.foundation)
                api(libs.compose.material3)
                api(libs.compose.ui)
                api(libs.compose.uiToolingPreview)

                api(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)

                implementation(libs.androidx.lifecycle.viewmodelCompose)
            }
        }
    }
}
