import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    android {
        namespace = "com.akrippner.mon.feature.library"
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
                implementation(projects.core.data)
                implementation(projects.core.designsystem)
                api(projects.core.arch)
                implementation(projects.core.model)
                api(libs.compose.runtime)
                api(libs.compose.foundation)
                api(libs.compose.material3)
                api(libs.compose.ui)
                api(libs.compose.uiToolingPreview)

                api(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)

                implementation(libs.androidx.lifecycle.viewmodelCompose)
            }
        }
    }
}
