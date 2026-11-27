import com.android.build.api.withAndroid
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
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

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    // Room has no browser target, so the database and the code on top of it are only wired in on
    // the JVM and Android (see PlatformModules).
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("jvmAndAndroid") {
                withJvm()
                withAndroid()
            }
        }
    }

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

    val jvmAndAndroidMain by getting {
        dependencies {
            implementation(projects.core.database)
        }
    }
}

}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}