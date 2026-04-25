import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
}

kotlin {


    android {
        namespace = "com.akrippner.mon.core.data"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
            minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":core:model"))
                api(libs.koin.core)
                implementation(libs.sudachi.core)
            }
        }
    }
}
