plugins {
    id("mon.kmp.library")
    id("mon.kmp.web")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.androidx.lifecycle.viewmodel)
                api(libs.kotlinx.coroutines.core)
            }
        }
    }
}
