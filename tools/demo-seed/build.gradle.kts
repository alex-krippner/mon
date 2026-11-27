import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Command-line tool, not part of the app: tokenizes the sample readings of the web demo and looks
// their words up, using the packs installed in ~/.mon/dictionaries (install `sudachi-core` and
// `jmdict-eng` in the desktop app first). Writes the seed the browser app starts with.
//
//   ./gradlew :tools:demo-seed:run --args="tools/demo-seed/readings.json \
//       webApp/src/commonMain/composeResources/files/demo-seed.json"
plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

application {
    mainClass.set("com.akrippner.mon.tools.demoseed.MainKt")
}

// So the paths in --args are relative to the repository, like the converter's.
tasks.named<JavaExec>("run") { workingDir = rootProject.projectDir }

dependencies {
    implementation(projects.core.demo)
    implementation(projects.core.dictionary)
    implementation(projects.core.packs)
    implementation(projects.core.tokenizer)
    implementation(projects.core.model)
    implementation(libs.koin.core)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
}
