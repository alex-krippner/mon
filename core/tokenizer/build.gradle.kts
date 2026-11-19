import com.android.build.api.withAndroid
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("mon.kmp.library")
}

kotlin {
    // Sudachi is a JVM library, so both targets share the code that uses it.
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
                implementation(libs.koin.core)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kermit)
                api(projects.core.model)
                implementation(projects.core.packs)
        }

        val jvmAndAndroidMain by getting {
            dependencies {
                implementation(libs.sudachi.core)
            }
        }

        jvmTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
