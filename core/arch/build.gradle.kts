plugins {
    id("mon.kmp.library")
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
