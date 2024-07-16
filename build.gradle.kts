import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("root.publication")
    id("module.publication")

    kotlin("multiplatform") version "2.0.0"

    id("io.gitlab.arturbosch.detekt") version "1.23.6"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    id("com.vanniktech.maven.publish") version "0.29.0"
}

group = "io.github.tokuhirom.kdary"
version = System.getenv("SDK_VERSION") ?: (
    "1.0.0" +
        if (hasProperty("release")) {
            ""
        } else {
            "-SNAPSHOT"
        }
)

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
    macosX64()

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
                implementation("com.squareup.okio:okio-nodefilesystem:3.9.0")
            }
        }
        val macosArm64Main by getting {
            dependencies {
                implementation("com.squareup.okio:okio:3.9.0")
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    if (project.hasProperty("mavenCentralUsername") ||
        System.getenv("ORG_GRADLE_PROJECT_mavenCentralUsername") != null
    ) {
        signAllPublications()
    }
}
