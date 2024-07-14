import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform") version "2.0.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

group = "me.geso.kdary"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
    }
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    jvm()
    js {
        nodejs {
            testTask {
                useMocha {
                    timeout = "10000" // 10 seconds timeout
                }
            }
        }
    }
    macosArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.squareup.okio:okio:3.9.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("com.squareup.okio:okio:3.9.0")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("com.squareup.okio:okio:3.9.0")
            }
        }
        val macosArm64Main by getting {
            dependencies {
                implementation("com.squareup.okio:okio:3.9.0")
            }
        }
    }
}
