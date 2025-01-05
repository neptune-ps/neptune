plugins {
    application
    kotlin("jvm")
}

version = "1.0.1-SNAPSHOT"

dependencies {
    api(project(":runescript-compiler")) {
        exclude("com.ibm.icu", "icu4j")
    }
    implementation(libs.netty.buffer)
    implementation(libs.fourkoma)
    implementation(libs.clikt)
    implementation(libs.gson)
    implementation(libs.logback)
}

application {
    applicationName = "cs2"
    mainClass.set("me.filby.neptune.clientscript.compiler.ClientScriptCompilerApplicationKt")
}
