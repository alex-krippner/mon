import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Command-line tool, not part of the app: turns dictionary source files into dictionary packs.
// Today the only source is JMdict (jmdict-simplified JSON files).
plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    application
}

// Same JVM target as the rest of the project; Java and Kotlin must agree.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

application {
    mainClass.set("com.akrippner.mon.tools.MainKt")
    // The whole JSON file (about 120 MB) is parsed into memory.
    applicationDefaultJvmArgs = listOf("-Xmx3g")
}

dependencies {
    implementation(projects.core.dictionary)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(kotlin("test"))
}
