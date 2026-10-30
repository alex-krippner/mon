import com.android.build.api.withAndroid
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
}

kotlin {


    android {
        namespace = "com.akrippner.mon.core.tokenizer"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    jvm()

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
                api(projects.core.model)
        }

        val jvmAndAndroidMain by getting {
            dependencies {
                implementation(libs.sudachi.core)
            }
        }
    }
}
