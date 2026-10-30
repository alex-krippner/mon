plugins {
    id("mon.kmp.library")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.core.model)
                implementation(projects.core.database)
                api(libs.kotlinx.coroutines.core)
                api(libs.koin.core)
            }
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
