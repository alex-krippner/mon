rootProject.name = "Mon"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":androidApp")
include(":desktopApp")
include(":shared")
include(":core:arch")
include(":core:domain")
include(":core:data")
include(":feature:library")
include(":feature:addreading")
include(":feature:dictionaries")
include(":feature:reader")
include(":feature:vocabulary")
include(":core:ui")
include(":core:model")
include(":core:designsystem")
include(":core:tokenizer")
include(":core:dictionary")
include(":core:packs")
include(":tools:pack-converter")
include(":core:database")
include(":core:navigation")