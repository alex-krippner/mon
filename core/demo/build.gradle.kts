plugins {
    id("mon.kmp.library")
    id("mon.kmp.web")
    alias(libs.plugins.kotlinSerialization)
}

// The sample readings and look-ups of the web demo. The seed tool (:tools:demo-seed) writes them on
// the JVM, where Sudachi and the JMdict pack exist, and the browser app reads them.
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)
            api(projects.core.dictionary)
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
