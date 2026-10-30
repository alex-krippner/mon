import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    android {
        namespace = "com.akrippner.mon.core.ui"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }


    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.compose.runtime)
                api(libs.compose.foundation)
                api(libs.compose.material3)
                api(libs.compose.ui)

                implementation(libs.compose.uiToolingPreview)
                implementation(projects.core.designsystem)
            }
        }
    }
}
