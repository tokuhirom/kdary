plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(rootProject)
    implementation("com.github.ajalt.clikt:clikt:5.0.0")
}

application {
    mainClass.set("io.github.tokuhirom.kdary.cli.mkkdary.MainKt")
}
