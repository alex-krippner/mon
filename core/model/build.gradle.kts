
plugins {
id("mon.kmp.library")
    id("mon.kmp.web")
}

kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
