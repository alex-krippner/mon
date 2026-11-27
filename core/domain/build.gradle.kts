plugins {
id("mon.kmp.library")
    id("mon.kmp.web")
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
                implementation(projects.core.dictionary)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kermit)
            }
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}


