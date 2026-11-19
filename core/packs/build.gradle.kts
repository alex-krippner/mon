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
            // PackReader still mentions Language and DictionarySource until phase 2.
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
