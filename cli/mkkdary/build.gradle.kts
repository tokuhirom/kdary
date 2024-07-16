plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(rootProject)
}

application {
    mainClass.set("io.github.tokuhirom.kdary.cli.mkdary.MainKt")
}
