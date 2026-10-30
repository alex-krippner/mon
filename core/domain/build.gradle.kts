plugins {
id("mon.kmp.library")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                // Domain layer dependencies
                implementation(libs.koin.core)
                implementation(projects.core.model)
                implementation(projects.core.data)
                implementation(projects.core.tokenizer)
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}


