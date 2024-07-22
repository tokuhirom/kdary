pluginManagement {
    includeBuild("convention-plugins")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "kdary"

include(":cli:mkkdary")
include(":cli:kdary")

include(":samples:longest-match")
include(":samples:momiji")
