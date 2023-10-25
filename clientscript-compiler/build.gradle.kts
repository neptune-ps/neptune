plugins {
    application
    kotlin("jvm")
}

dependencies {
    api(project(":runescript-compiler")) {
        exclude("com.ibm.icu", "icu4j")
    }
    implementation(libs.netty.buffer)
    implementation(libs.fourkoma)
    runtimeOnly(libs.logback)
}

application {
    applicationName = "cs2"
    mainClass.set("me.filby.neptune.clientscript.compiler.ClientScriptCompilerApplicationKt")
}
