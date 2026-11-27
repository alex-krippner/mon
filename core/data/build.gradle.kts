import com.android.build.api.withAndroid
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("mon.kmp.library")
    id("mon.kmp.web")
}

kotlin {
    // Room has no browser target, so the Room repository and everything that maps database rows
    // live in the code the JVM and Android share. The browser gets InMemoryReadingRepository.
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("jvmAndAndroid") {
                withJvm()
                withAndroid()
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)
            api(libs.kotlinx.coroutines.core)
            api(libs.koin.core)
        }

        val jvmAndAndroidMain by getting {
            dependencies {
                implementation(projects.core.database)
            }
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
