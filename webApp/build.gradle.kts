import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

// The browser app. Room, Sudachi and the pack files have no browser version, so this is a demo: it
// keeps the readings in memory and starts with the ones from the demo seed.
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "mon"
        browser {
            commonWebpackConfig {
                outputFileName = "mon.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.components.resources)
        }
        wasmJsMain.dependencies {
            implementation(projects.shared)
            implementation(projects.core.data)
            implementation(projects.core.demo)
            implementation(projects.core.dictionary)
            implementation(projects.core.model)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

compose.resources {
    packageOfResClass = "com.akrippner.mon.webapp.generated.resources"
    generateResClass = always
}
