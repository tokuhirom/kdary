plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(rootProject)
    implementation("com.github.ajalt.clikt:clikt:4.4.0")

    implementation("io.ktor:ktor-client-core:2.0.0")
    implementation("io.ktor:ktor-client-cio:2.0.0")
    implementation("io.ktor:ktor-client-content-negotiation:2.0.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.0.0")
}

application {
    mainClass.set("io.github.tokuhirom.kdary.samples.longestmatch.MainKt")
}
