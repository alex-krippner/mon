plugins { `kotlin-dsl` }

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "mon.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
    }
}