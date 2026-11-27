plugins { `kotlin-dsl` }

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.kotlin.composeCompilerGradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "mon.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("kmpCompose") {
            id = "mon.kmp.compose"
            implementationClass = "KmpComposeConventionPlugin"
        }
        register("feature") {
            id = "mon.feature"
            implementationClass = "FeatureConventionPlugin"
        }
        register("kmpWeb") {
            id = "mon.kmp.web"
            implementationClass = "KmpWebConventionPlugin"
        }
    }
}