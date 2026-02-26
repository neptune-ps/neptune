plugins {
    application
    kotlin("jvm")
}

version = "1.0.1-SNAPSHOT"

dependencies {
    api(project(":runescript-compiler"))
    implementation(libs.netty.buffer)
    implementation(libs.fourkoma)
    implementation(libs.clikt)
    implementation(libs.gson) {
        exclude("com.google.errorprone", "error_prone_annotations")
    }
    implementation(libs.logback)
}

application {
    applicationName = "cs2"
    mainClass.set("me.filby.neptune.clientscript.compiler.ClientScriptCompilerApplicationKt")
}
