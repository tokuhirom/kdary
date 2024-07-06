plugins {
    kotlin("multiplatform") version "2.0.0"
    id("io.gitlab.arturbosch.detekt") version("1.23.6")
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

group = "me.geso.dartsclonekt"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
    }
}

kotlin {
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.squareup.okio:okio:3.9.0")
            }
        }
    }
}
