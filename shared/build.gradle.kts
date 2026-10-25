import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android{
        namespace = "com.akrippner.mon"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        androidResources {
            enable = true
        }
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }

    }

    jvm()

//    listOf(
//        iosArm64(),
//        iosSimulatorArm64()
//    ).forEach { iosTarget ->
//        iosTarget.binaries.framework {
//            baseName = "ComposeApp"
//            isStatic = true
//        }
//    }
//
//
//    js {
//        browser()
//    }
//
//    @OptIn(ExperimentalWasmDsl::class)
//    wasmJs {
//        browser()
//    }
sourceSets {

    commonMain.dependencies {
        implementation(project(":core:navigation"))
        implementation(project(":core:domain"))
        implementation(project(":feature:library"))
        implementation(project(":core:data"))
        implementation(project(":core:ui"))
        implementation(project(":core:designsystem"))
        implementation(project(":core:tokenizer"))
        implementation(project(":core:database"))

        implementation(libs.compose.runtime)
        implementation(libs.compose.foundation)
        implementation(libs.compose.material3)
        implementation(libs.compose.ui)
        implementation(libs.compose.components.resources)
        implementation(libs.compose.uiToolingPreview)
        implementation(libs.androidx.lifecycle.runtimeCompose)
        api(libs.koin.core)
        implementation(libs.koin.compose)
        implementation(libs.navigation3.ui)
        implementation(libs.kotlinx.serialization.json)

    }
}

}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}