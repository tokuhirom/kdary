plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(rootProject)
    implementation("com.github.ajalt.clikt:clikt:5.0.1")

    implementation("io.ktor:ktor-client-core:3.0.1")
    implementation("io.ktor:ktor-client-cio:3.0.1")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.1")

    implementation("com.squareup.okio:okio:3.9.1")
}

application {
    mainClass.set("io.github.tokuhirom.kdary.samples.momiji.MainKt")
    applicationDefaultJvmArgs = listOf("-Xmx2g")
}
