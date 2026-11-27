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
            jvmTarget.set(JvmTarget.JVM_17)
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
        implementation(projects.core.navigation)
        implementation(projects.core.domain)
        implementation(projects.feature.library)
        implementation(projects.feature.addreading)
        implementation(projects.feature.dictionaries)
        implementation(projects.feature.reader)
        implementation(projects.feature.vocabulary)
        implementation(projects.core.data)
        implementation(projects.core.ui)
        implementation(projects.core.designsystem)
        implementation(projects.core.tokenizer)
        implementation(projects.core.dictionary)
        implementation(projects.core.packs)
        implementation(projects.core.database)
        implementation(libs.kermit)

        implementation(libs.compose.runtime)
        implementation(libs.compose.foundation)
        implementation(libs.compose.material3)
        implementation(libs.compose.ui)
        implementation(libs.compose.components.resources)
        implementation(libs.compose.uiToolingPreview)
        implementation(libs.androidx.lifecycle.runtimeCompose)
        api(libs.koin.core)
        implementation(libs.koin.compose)
        implementation(libs.koin.compose.viewmodel)
        implementation(libs.navigation3.ui)
        implementation(libs.androidx.lifecycle.viewmodelNavigation3)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.kotlinx.coroutines.core)

    }
}

}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}