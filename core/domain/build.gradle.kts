import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
}

kotlin {


    android {
        namespace = "com.akrippner.mon.core.domain"
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
                // Domain layer dependencies
                implementation(libs.koin.core)
                implementation(project(":core:model"))
                implementation(project(":core:data"))
                implementation(project(":core:tokenizer"))
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}


