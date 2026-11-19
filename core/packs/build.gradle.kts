import com.android.build.api.withAndroid
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("mon.kmp.library")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
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
            // PackFormats checks the languages of a pack with Language from :core:model.
            api(projects.core.model)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }

        val jvmAndAndroidMain by getting {
            dependencies {
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.okhttp)
            }
        }
    }
}
