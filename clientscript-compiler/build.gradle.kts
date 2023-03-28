plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":runescript-compiler"))
    implementation(libs.netty.buffer)
    runtimeOnly(libs.logback)
}
