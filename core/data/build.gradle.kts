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
                api(projects.core.model)
                implementation(projects.core.database)
                api(libs.kotlinx.coroutines.core)
                api(libs.koin.core)
            }
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
