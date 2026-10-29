import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.ksp)
}

kotlin {
    android {
        namespace = "com.akrippner.mon.core.database"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        jvmTest.dependencies {
            implementation(libs.androidx.room.testing)
        }

        androidMain.dependencies {
            // Optional when using Room SQLite Wrapper
            implementation(libs.androidx.room.sqlite.wrapper)
        }
    }
}

dependencies {
//    add("kspAndroid", libs.androidx.room.compiler)
//    add("kspJvm", libs.androidx.room.compiler)
////    add("kspAndroidTest", libs.androidx.room.compiler)
//    add("kspJvmTest", libs.androidx.room.compiler)

    configurations
        .filter { it.name.startsWith("ksp")}
        .forEach {
            add(it.name, libs.androidx.room.compiler)
        }
}

room {
    schemaDirectory("$projectDir/schemas")
}

configurations.forEach {
    if (it.name.contains("ksp", ignoreCase = true)) {
        println("KSP Configuration found: ${it.name}")
    }
}